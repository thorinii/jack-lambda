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

package com.android.jack.jayce.v0002.nodes;

import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java local variable definition.
 */
public class NLocal extends NVariable {

  @Nonnull
  public static final Token TOKEN = Token.LOCAL;

  @CheckForNull
  public String id;

  public int modifiers;

  @CheckForNull
  public String type;

  @CheckForNull
  public String name;

  @Nonnull
  public List<NAnnotationLiteral> annotationSet = Collections.emptyList();

  @Nonnull
  public List<NMarker> markers = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;


  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JLocal jLocal = (JLocal) node;
    id = loader.getLocalSymbols().getId(jLocal);
    modifiers = jLocal.getModifier();
    type = ImportHelper.getSignatureName(jLocal.getType());
    name = jLocal.getName();
    annotationSet = loader.load(NAnnotationLiteral.class, jLocal.getAnnotations());
    markers = loader.load(NMarker.class, jLocal.getAllMarkers());
    sourceInfo = loader.load(jLocal.getSourceInfo());
  }

  @Override
  @Nonnull
  public JLocal exportAsJast(@Nonnull ExportSession exportSession) throws JTypeLookupException,
      JMethodLookupException {
    assert sourceInfo != null;
    assert type != null;
    assert id != null;
    JLocal jLocal = new JLocal(
        sourceInfo.exportAsJast(exportSession),
        name,
        exportSession.getLookup().getType(type),
        modifiers,
        null); /* methodBody */
    exportSession.getLocalResolver().addTarget(id, jLocal);
    for (NAnnotationLiteral annotation : annotationSet) {
      jLocal.addAnnotation(annotation.exportAsJast(exportSession));
    }
    for (NMarker marker : markers) {
      jLocal.addMarker(marker.exportAsJast(exportSession));
    }
    return jLocal;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(id);
    out.writeInt(modifiers);
    out.writeId(type);
    out.writeId(name);
    out.writeNodes(annotationSet);
    out.writeNodes(markers);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    id = in.readId();
    modifiers = in.readInt();
    type = in.readId();
    name = in.readId();
    annotationSet = in.readNodes(NAnnotationLiteral.class);
    markers = in.readNodes(NMarker.class);

  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }

  @Override
  @Nonnull
  public NSourceInfo getSourceInfos() {
    assert sourceInfo != null;
    return sourceInfo;
  }

  @Override
  public void setSourceInfos(@Nonnull NSourceInfo sourceInfo) {
    this.sourceInfo = sourceInfo;
  }
}
