/*
 *  Copyright (c) 2012 Sam Harwell, Tunnel Vision Laboratories LLC
 *  All rights reserved.
 *
 *  The source code of this document is proprietary work, and is not licensed for
 *  distribution. For information about licensing, contact Sam Harwell at:
 *      sam@tunnelvisionlabs.com
 */
package org.tvl.goworks.editor.go.codemodel;

import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Sam Harwell
 */
public interface FileModel extends CodeElementModel {

    // allow multiples to improve ability to recover from syntax errors
    @NonNull
    Collection<? extends PackageDeclarationModel> getPackageDeclarations();

    @NonNull
    Collection<? extends ImportDeclarationModel> getImportDeclarations();

    @NonNull
    Collection<? extends TypeModel> getTypes();

    @NonNull
    Collection<? extends TypeModel> getTypes(String name);

    @NonNull
    Collection<? extends ConstModel> getConstants();

    @NonNull
    Collection<? extends ConstModel> getConstants(String name);

    @NonNull
    Collection<? extends VarModel> getVars();

    @NonNull
    Collection<? extends VarModel> getVars(String name);

    @NonNull
    Collection<? extends FunctionModel> getFunctions();

    @NonNull
    Collection<? extends FunctionModel> getFunctions(String name);

}
