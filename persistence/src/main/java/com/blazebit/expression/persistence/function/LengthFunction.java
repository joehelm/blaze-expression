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
import com.blazebit.expression.ExpressionInterpreter;
import com.blazebit.expression.persistence.DocumentationMetadataDefinition;
import com.blazebit.expression.persistence.FunctionRenderer;
import com.blazebit.expression.persistence.PersistenceExpressionSerializer;
import com.blazebit.expression.spi.FunctionInvoker;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Consumer;

import static com.blazebit.expression.persistence.PersistenceDomainContributor.INTEGER;
import static com.blazebit.expression.persistence.PersistenceDomainContributor.STRING;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class LengthFunction implements FunctionRenderer, FunctionInvoker, Serializable {

    private static final LengthFunction INSTANCE = new LengthFunction();

    private LengthFunction() {
    }

    /**
     * Adds the LENGTH function to the domain builder.
     *
     * @param domainBuilder The domain builder
     */
    public static void addFunction(DomainBuilder domainBuilder) {
        domainBuilder.createFunction("LENGTH")
                .withMetadata(new FunctionRendererMetadataDefinition(INSTANCE))
                .withMetadata(new FunctionInvokerMetadataDefinition(INSTANCE))
                .withMetadata(DocumentationMetadataDefinition.localized("LENGTH"))
                .withExactArgumentCount(1)
                .withResultType(INTEGER)
                .withArgument("string", STRING, DocumentationMetadataDefinition.localized("LENGTH_STRING"))
                .build();
    }

    @Override
    public Object invoke(ExpressionInterpreter.Context context, DomainFunction function, Map<DomainFunctionArgument, Object> arguments) {
        Object string = arguments.get(function.getArgument(0));
        if (string == null) {
            return null;
        }

        String s = string.toString();
        return BigInteger.valueOf(s.length());
    }

    @Override
    public void render(DomainFunction function, DomainType returnType, Map<DomainFunctionArgument, Consumer<StringBuilder>> argumentRenderers, StringBuilder sb, PersistenceExpressionSerializer serializer) {
        sb.append("LENGTH(");
        argumentRenderers.values().iterator().next().accept(sb);
        sb.append(')');
    }
}
