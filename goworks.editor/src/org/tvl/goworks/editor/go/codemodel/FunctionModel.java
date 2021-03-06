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
public interface FunctionModel extends CodeElementModel {

    boolean isMethod();

    @NonNull
    Collection<? extends ParameterModel> getParameters();

    @NonNull
    Collection<? extends ParameterModel> getReturnValues();

    ParameterModel getReceiverParameter();

}
