/*
 * Copyright 2019 - 2020 Blazebit.
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

package com.blazebit.expression.persistence.function;

import com.blazebit.domain.boot.model.DomainBuilder;
import com.blazebit.domain.runtime.model.DomainFunction;
import com.blazebit.domain.runtime.model.DomainFunctionArgument;
import com.blazebit.domain.runtime.model.DomainType;
import com.blazebit.domain.runtime.model.StaticDomainFunctionTypeResolvers;
import com.blazebit.expression.ExpressionInterpreter;
import com.blazebit.expression.persistence.DocumentationMetadataDefinition;
import com.blazebit.expression.persistence.FunctionRenderer;
import com.blazebit.expression.persistence.PersistenceDomainContributor;
import com.blazebit.expression.persistence.PersistenceExpressionSerializer;
import com.blazebit.expression.spi.FunctionInvoker;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CeilFunction implements FunctionRenderer, FunctionInvoker, Serializable {

    private static final CeilFunction INSTANCE = new CeilFunction();

    private CeilFunction() {
    }

    /**
     * Adds the CEIL function to the domain builder.
     *
     * @param domainBuilder The domain builder
     */
    public static void addFunction(DomainBuilder domainBuilder) {
        domainBuilder.createFunction("CEIL")
                .withMetadata(new FunctionRendererMetadataDefinition(INSTANCE))
                .withMetadata(new FunctionInvokerMetadataDefinition(INSTANCE))
                .withMetadata(DocumentationMetadataDefinition.localized("CEIL"))
                .withExactArgumentCount(1)
                .withArgument("number", DocumentationMetadataDefinition.localized("CEIL_ARG"))
                .build();
        domainBuilder.withFunctionTypeResolver("CEIL", StaticDomainFunctionTypeResolvers.returning(PersistenceDomainContributor.INTEGER));
    }

    @Override
    public Object invoke(ExpressionInterpreter.Context context, DomainFunction function, Map<DomainFunctionArgument, Object> arguments) {
        Object argument = arguments.get(function.getArgument(0));
        if (argument == null) {
            return null;
        }

        if (argument instanceof Number) {
            return BigInteger.valueOf((long) Math.ceil(((Number) argument).doubleValue()));
        } else {
            throw new IllegalArgumentException("Illegal argument for CEIL function: " + argument);
        }
    }

    @Override
    public void render(DomainFunction function, DomainType returnType, Map<DomainFunctionArgument, Consumer<StringBuilder>> argumentRenderers, StringBuilder sb, PersistenceExpressionSerializer serializer) {
        sb.append("CEIL(");
        argumentRenderers.values().iterator().next().accept(sb);
        sb.append(')');
    }
}
