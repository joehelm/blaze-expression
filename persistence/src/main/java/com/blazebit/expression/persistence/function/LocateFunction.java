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
import com.blazebit.expression.persistence.PersistenceExpressionSerializer;
import com.blazebit.expression.spi.FunctionInvoker;
import com.blazebit.expression.persistence.FunctionRenderer;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

import static com.blazebit.expression.persistence.PersistenceDomainContributor.INTEGER;
import static com.blazebit.expression.persistence.PersistenceDomainContributor.STRING;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class LocateFunction implements FunctionRenderer, FunctionInvoker, Serializable {

    private static final LocateFunction INSTANCE = new LocateFunction();

    private LocateFunction() {
    }

    /**
     * Adds the LOCATE function to the domain builder.
     *
     * @param domainBuilder The domain builder
     */
    public static void addFunction(DomainBuilder domainBuilder) {
        domainBuilder.createFunction("LOCATE")
                .withMetadata(new FunctionRendererMetadataDefinition(INSTANCE))
                .withMetadata(new FunctionInvokerMetadataDefinition(INSTANCE))
                .withMetadata(DocumentationMetadataDefinition.localized("LOCATE"))
                .withMinArgumentCount(2)
                .withResultType(INTEGER)
                .withArgument("substring", STRING, DocumentationMetadataDefinition.localized("LOCATE_SUBSTRING"))
                .withArgument("string", STRING, DocumentationMetadataDefinition.localized("LOCATE_STRING"))
                .withArgument("start", INTEGER, DocumentationMetadataDefinition.localized("LOCATE_START"))
                .build();
    }

    @Override
    public Object invoke(ExpressionInterpreter.Context context, DomainFunction function, Map<DomainFunctionArgument, Object> arguments) {
        Object substring = arguments.get(function.getArgument(0));
        if (substring == null) {
            return null;
        }
        Object string = arguments.get(function.getArgument(1));
        if (string == null) {
            return null;
        }
        Object start = arguments.getOrDefault(function.getArgument(2), 0);
        if (start == null) {
            return null;
        }

        String needle = substring.toString();
        String s = string.toString();
        int startIndex = ((Number) start).intValue();
        return s.indexOf(needle, startIndex);
    }

    @Override
    public void render(DomainFunction function, DomainType returnType, Map<DomainFunctionArgument, Consumer<StringBuilder>> argumentRenderers, StringBuilder sb, PersistenceExpressionSerializer serializer) {
        sb.append("LOCATE(");
        argumentRenderers.get(function.getArgument(0)).accept(sb);
        sb.append(", ");
        argumentRenderers.get(function.getArgument(1)).accept(sb);
        Consumer<StringBuilder> thirdArg = argumentRenderers.get(function.getArgument(2));
        if (thirdArg != null) {
            sb.append(", ");
            thirdArg.accept(sb);
        }
        sb.append(')');
    }
}
