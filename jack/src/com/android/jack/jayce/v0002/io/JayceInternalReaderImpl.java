/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.jayce.v0002.io;

import com.android.jack.jayce.DeclaredTypeNode;
import com.android.jack.jayce.JayceFormatException;
import com.android.jack.jayce.JayceInternalReader;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0002.NNode;
import com.android.jack.jayce.v0002.Version;
import com.android.jack.jayce.v0002.nodes.HasCatchBlockIds;
import com.android.jack.jayce.v0002.nodes.HasSourceInfo;
import com.android.jack.jayce.v0002.nodes.NDeclaredType;
import com.android.jack.jayce.v0002.nodes.NMethod;
import com.android.jack.jayce.v0002.nodes.NSourceInfo;
import com.android.jack.jayce.v0002.util.DispatchKindIdHelper;
import com.android.jack.jayce.v0002.util.FieldRefKindIdHelper;
import com.android.jack.jayce.v0002.util.MethodKindIdHelper;
import com.android.jack.jayce.v0002.util.ReceiverKindIdHelper;
import com.android.jack.jayce.v0002.util.RetentionPolicyIdHelper;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.PercentImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Jayce internal reader implementation.
 */
public class JayceInternalReaderImpl implements JayceInternalReader {
  @Nonnull
  public static final StatisticId<Percent> SKIPED_NDECLARED_TYPE = new StatisticId<Percent>(
      "jayce.ndeclaredtype.skiped", "NDeclaredType loading that skiped by the reader",
      PercentImpl.class, Percent.class);
  @Nonnull
  public static final StatisticId<Percent> SKIPED_BODY = new StatisticId<Percent>(
      "jayce.body.skiped", "Body loading skiped by the reader",
      PercentImpl.class, Percent.class);

  @Nonnull
  private final Tokenizer tokenizer;

  @Nonnull
  private NodeLevel nodeLevel = NodeLevel.FULL;

  @CheckForNull
  private NDeclaredType type;

  @CheckForNull
  private String currentFileName;

  @Nonnegative
  private int currentLine;

  @Nonnull
  private final List<String> currentCatchBlockList = new ArrayList<String>();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  public JayceInternalReaderImpl(@Nonnull InputStream in) {
    this.tokenizer = new Tokenizer(in);
  }

  @Nonnull
  public NodeLevel getNodeLevel() {
    return nodeLevel;
  }

  @CheckForNull
  public String readId() throws IOException {
    return readString();
  }

  @CheckForNull
  public String readCurrentFileName() throws IOException {
    if (tokenizer.readOpenFileName()) {
      currentFileName = readString();
      tokenizer.readCloseFileName();
    }
    return currentFileName;
  }

  @Nonnegative
  public int readCurrentLine() throws IOException {
    if (tokenizer.readOpenLineInfo()) {
      currentLine = readInt();
      tokenizer.readCloseLineInfo();
    }
    return currentLine;
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <T extends Enum<T>> T readRetentionPolicyEnum() throws IOException {
    return (T) RetentionPolicyIdHelper.getValue(readByte());
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <T extends Enum<T>> T readFieldRefKindEnum() throws IOException {
    return (T) FieldRefKindIdHelper.getValue(readByte());
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <T extends Enum<T>> T readMethodKindEnum() throws IOException {
    return (T) MethodKindIdHelper.getValue(readByte());
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <T extends Enum<T>> T readReceiverKindEnum() throws IOException {
    return (T) ReceiverKindIdHelper.getValue(readByte());
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <T extends Enum<T>> T readDispatchKindEnum() throws IOException {
    return (T) DispatchKindIdHelper.getValue(readByte());
  }

  @CheckForNull
  public String readString() throws IOException {
    return tokenizer.readString();
  }

  @Nonnull
  public List<String> readIds() throws IOException {
    tokenizer.readOpen();
    int length = readInt();
    List<String> ids = new ArrayList<String>(length);
    for (int i = 0; i < length; i++) {
      ids.add(readId());
    }
    tokenizer.readClose();
    return ids;
  }

  public void readCatchBlockIds() throws IOException {
    if (tokenizer.readOpenCatchBlockIdAdd()) {
      int length = tokenizer.readInt();
      for (int i = 0; i < length; i++) {
        String id = readId();
        currentCatchBlockList.add(id);
        assert currentCatchBlockList.indexOf(id) == currentCatchBlockList.lastIndexOf(id);
      }
      tokenizer.readCloseCatchBlockId();
    }
    if (tokenizer.readOpenCatchBlockIdRemove()) {
      int length = tokenizer.readInt();
      for (int i = 0; i < length; i++) {
        String id = readId();
        currentCatchBlockList.remove(id);
        assert !currentCatchBlockList.contains(id);
      }
      tokenizer.readCloseCatchBlockId();
    }
  }

  @SuppressWarnings("unchecked")
  @CheckForNull
  public <T extends NNode> T readNode(@Nonnull Class<T> nodeClass) throws IOException,
      JayceFormatException {
    String fileName = readCurrentFileName();
    int startLine = readCurrentLine();

    readCatchBlockIds();

    Token token = tokenizer.next();

    if (token == Token.NULL) {
      return null;
    }



    tokenizer.readOpen();
    NNode node;
    try {
      node = token.newNode();
    } catch (InvalidTokenException e) {
      throw new ParseException("Unexpected token " + token + " while expecting node.", e);
    }
    Percent statistic = null;
    if (token == Token.METHOD_BODY) {
      statistic = tracer.getStatistic(SKIPED_BODY);
    } else if (node instanceof NDeclaredType) {
      statistic = tracer.getStatistic(SKIPED_NDECLARED_TYPE);
    }

    if (!nodeClass.isAssignableFrom(node.getClass())) {
      throw new JayceFormatException("Unexpected node " + node.getClass().getSimpleName() + ", "
          + nodeClass.getSimpleName() + " was expected.");
    }

    if (node instanceof HasSourceInfo) {
      NSourceInfo sourceInfo = new NSourceInfo();
      sourceInfo.fileName = fileName;
      sourceInfo.startLine = startLine;
      ((HasSourceInfo) node).setSourceInfos(sourceInfo);
    }
    if (node instanceof HasCatchBlockIds) {
      ((HasCatchBlockIds) node).setCatchBlockIds(new ArrayList<String>(currentCatchBlockList));
    }
    node.readContent(this);
    readSourceInfoEnd(node);
    assert !(node instanceof NMethod) || currentCatchBlockList.isEmpty();
    tokenizer.readClose();

    if (nodeLevel.keep(token.getNodeLevel())) {
      if (statistic != null) {
        statistic.addFalse();
      }
      return (T) node;
    } else {
      if (statistic != null) {
        statistic.addTrue();
      }
      return null;
    }
  }

  private void readSourceInfoEnd(@Nonnull NNode node)
      throws IOException {
    if (node instanceof HasSourceInfo) {
      NSourceInfo sourceInfo = ((HasSourceInfo) node).getSourceInfos();
      sourceInfo.endLine = readCurrentLine();
      if (sourceInfo.startLine == 0
              && sourceInfo.endLine == 0
              && !(node instanceof NDeclaredType)) {
        ((HasSourceInfo) node).setSourceInfos(NSourceInfo.UNKNOWN);
      }
    }
  }

  @Nonnull
  public <T extends NNode> List<T> readNodes(@Nonnull Class<T> nodeClass) throws IOException,
      JayceFormatException {
    tokenizer.readOpen();
    int length = readInt();
    List<T> nodes = new ArrayList<T>(length);
    for (int i = 0; i < length; i++) {
      T node = readNode(nodeClass);
      if (node != null) {
        nodes.add(node);
      }
    }
    tokenizer.readClose();
    return nodes;

  }

  public int readInt() throws IOException {
    return tokenizer.readInt();
  }

  public byte readByte() throws IOException {
    return tokenizer.readByte();
  }

  public boolean readBoolean() throws IOException {
    return tokenizer.readBoolean();
  }

  public long readLong() throws IOException {
    return tokenizer.readLong();
  }

  public short readShort() throws IOException {
    return tokenizer.readShort();
  }

  public char readChar() throws IOException {
    return tokenizer.readChar();
  }

  public float readFloat() throws IOException {
    return tokenizer.readFloat();
  }

  public double readDouble() throws IOException {
    return tokenizer.readDouble();
  }

  @Override
  @Nonnull
  public DeclaredTypeNode readType(@Nonnull NodeLevel nodeLevel) throws IOException,
      JayceFormatException {
    if (type == null) {
      this.nodeLevel = nodeLevel;
      type = readNode(NDeclaredType.class);
    }
    assert type != null;
    return type;
  }

  @Override
  public int getCurrentMinor() {
    return Version.CURRENT_MINOR;
  }

  @Override
  public int getMinorMin() {
    return Version.MINOR_MIN;
  }
}
