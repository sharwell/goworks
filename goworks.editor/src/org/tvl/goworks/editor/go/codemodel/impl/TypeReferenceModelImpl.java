/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.tvl.goworks.editor.go.codemodel.impl;

import java.util.Collection;
import java.util.Collections;
import org.tvl.goworks.editor.go.codemodel.TypeKind;
import org.tvl.goworks.editor.go.codemodel.TypeReferenceModel;

/**
 *
 * @author Sam Harwell
 */
public class TypeReferenceModelImpl extends TypeModelImpl implements TypeReferenceModel {
    private final String referencedPackageName;
    private final String referencedTypeName;

    public TypeReferenceModelImpl(String referencedPackageName, String referencedTypeName, FileModelImpl fileModel) {
        super(getQualifiedName(referencedPackageName, referencedTypeName), fileModel);
        this.referencedPackageName = referencedPackageName;
        this.referencedTypeName = referencedTypeName;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.UNKNOWN;
    }

    private static String getQualifiedName(String packageName, String typeName) {
        if (packageName == null || packageName.isEmpty()) {
            return typeName;
        }

        return packageName + "." + typeName;
    }

    @Override
    public TypeModelImpl resolve() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends AbstractCodeElementModel> getMembers() {
        TypeModelImpl resolved = resolve();
        if (resolved != null) {
            return resolved.getMembers();
        }

        return Collections.emptyList();
    }
}
