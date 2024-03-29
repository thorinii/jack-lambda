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

package com.android.jack.backend.jayce;

import com.android.jack.Jack;
import com.android.jack.JackEventType;
import com.android.jack.config.id.Arzon;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.Resource;
import com.android.jack.library.FileType;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.lookup.JLookup;
import com.android.sched.util.HasDescription;
import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Imports jayce files from jack libraries in J-AST.
 */
@HasKeyId
public class JayceFileImporter {

  @Nonnull
  public static final String JAYCE_FILE_EXTENSION = ".jayce";

  public static final int JACK_EXTENSION_LENGTH = JAYCE_FILE_EXTENSION.length();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final List<InputJackLibrary> jackLibraries;

  private static final char VPATH_SEPARATOR = JLookup.PACKAGE_SEPARATOR;

  private enum CollisionPolicy implements HasDescription {
    KEEP_FIRST("keep the first element encountered"),
    FAIL("fail when a collision occurs");

    @Nonnull
    private String description;

    private CollisionPolicy(@Nonnull String description) {
      this.description = description;
    }

    @Override
    @Nonnull
    public String getDescription() {
      return description;
    }
  }

  @Nonnull
  public static final PropertyId<CollisionPolicy> COLLISION_POLICY = PropertyId.create(
      "jack.import.jackfile.policy",
      "Defines the policy to follow concerning type collision from imported jayce files",
      new EnumCodec<CollisionPolicy>(CollisionPolicy.values()).ignoreCase())
      .addDefaultValue(CollisionPolicy.FAIL);

  @Nonnull
  private final CollisionPolicy collisionPolicy = ThreadConfig.get(COLLISION_POLICY);

  @Nonnull
  public static final PropertyId<CollisionPolicy> RESOURCE_COLLISION_POLICY = PropertyId.create(
      "jack.import.resource.policy",
      "Defines the policy to follow concerning resource collision in imported jack containers",
      new EnumCodec<CollisionPolicy>(CollisionPolicy.values()).ignoreCase())
      .addDefaultValue(CollisionPolicy.FAIL).withCategory(Arzon.get());

  @Nonnull
  private final CollisionPolicy resourceCollisionPolicy =
      ThreadConfig.get(RESOURCE_COLLISION_POLICY);

  public JayceFileImporter(@Nonnull List<InputJackLibrary> jackLibraries) {
    this.jackLibraries = jackLibraries;
  }

  public void doImport(@Nonnull JSession session) throws JPackageLookupException,
      ImportConflictException, JTypeLookupException {

    for (InputJackLibrary jackLibrary : jackLibraries) {
      logger.log(Level.FINE, "Importing {0}", jackLibrary.getLocation().getDescription());
      Iterator<InputVFile> jayceFileIt = jackLibrary.iterator(FileType.JAYCE);
      while (jayceFileIt.hasNext()) {
        InputVFile jayceFile = jayceFileIt.next();
        String name = getNameFromInputVFile(jackLibrary, jayceFile, FileType.JAYCE);
        addImportedTypes(session, name, jackLibrary);
      }

      Iterator<InputVFile> rscFileIt = jackLibrary.iterator(FileType.RSC);
      while (rscFileIt.hasNext()) {
        InputVFile rscFile = rscFileIt.next();
        String name = getNameFromInputVFile(jackLibrary, rscFile, FileType.RSC);
        addImportedResource(rscFile, session, name);
      }
    }
  }

  // TODO(jack-team): remove this hack
  @Nonnull
  private String getNameFromInputVFile(@Nonnull InputJackLibrary jackLibrary,
      @Nonnull InputVFile jayceFile, @Nonnull FileType fileType) {
    Location loc = jayceFile.getLocation();
    String name;
    if (loc instanceof ZipLocation) {
      name = ((ZipLocation) jayceFile.getLocation()).getEntryName();
      if (jackLibrary.getMajorVersion() != 0) {
        name = name.substring(
            fileType.buildDirVPath(VPath.ROOT).split().iterator().next().length() + 1);
      }
    } else {
      name = ((FileLocation) jayceFile.getLocation()).getPath();
      if (jackLibrary.getMajorVersion() != 0) {
        String prefix = fileType.buildDirVPath(VPath.ROOT).split().iterator().next() + '/';
        name = name.substring(name.lastIndexOf(prefix) + prefix.length());
      }
    }
    return name;
  }

  private void addImportedTypes(@Nonnull JSession session, @Nonnull String path,
      @Nonnull InputLibrary intendedInputLibrary) throws TypeImportConflictException,
      JTypeLookupException {
    Event readEvent = tracer.start(JackEventType.NNODE_READING_FOR_IMPORT);
    try {
      logger.log(Level.FINEST, "Importing jayce file ''{0}'' from {1}", new Object[] {path,
          intendedInputLibrary.getLocation().getDescription()});
      String signature = convertJackFilePathToSignature(path);
      JDefinedClassOrInterface declaredType =
          (JDefinedClassOrInterface) session.getLookup().getType(signature);
      Location existingSource = declaredType.getLocation();
      if (!(existingSource instanceof TypeInInputLibraryLocation) ||
          ((TypeInInputLibraryLocation) existingSource).getInputLibraryLocation().getInputLibrary()
          != intendedInputLibrary) {
        if (collisionPolicy == CollisionPolicy.FAIL) {
          throw new TypeImportConflictException(declaredType, intendedInputLibrary.getLocation());
        } else {
          session.getUserLogger().log(Level.INFO,
              "Type ''{0}'' from {1} has already been imported from {2}: "
              + "ignoring import", new Object[] {
              Jack.getUserFriendlyFormatter().getName(declaredType),
              intendedInputLibrary.getLocation().getDescription(),
              existingSource.getDescription()});
        }
      } else {
        session.addTypeToEmit(declaredType);
      }
    } finally {
      readEvent.end();
    }
  }

  @Nonnull
  private String convertJackFilePathToSignature(@Nonnull String path) {
    String pathWithoutExt = path.substring(0, path.length() - JAYCE_FILE_EXTENSION.length());
    return "L" + pathWithoutExt.replace(VPATH_SEPARATOR, JLookup.PACKAGE_SEPARATOR) + ";";
  }

  private void addImportedResource(@Nonnull InputVFile file, @Nonnull JSession session,
      @Nonnull String currentPath) throws ResourceImportConflictException {
    VPath path = new VPath(currentPath, VPATH_SEPARATOR);
    Resource newResource = new Resource(path, file);
    for (Resource existingResource : session.getResources()) {
      if (existingResource.getPath().equals(path)) {
        if (resourceCollisionPolicy == CollisionPolicy.FAIL) {
          throw new ResourceImportConflictException(newResource.getLocation(),
              existingResource.getLocation());
        } else {
          session.getUserLogger().log(Level.INFO,
              "Resource in {0} has already been imported from {1}: ignoring import", new Object[] {
                  newResource.getLocation().getDescription(),
                  existingResource.getLocation().getDescription()});
        }
        return;
      }
    }
    session.addResource(newResource);
  }

  public static boolean isJackFileName(@Nonnull String name) {
    return (name.length() > JACK_EXTENSION_LENGTH) && (name.substring(
        name.length() - JACK_EXTENSION_LENGTH).equalsIgnoreCase(JAYCE_FILE_EXTENSION));
  }
}
