/*
 * Copyright 2019 Blazebit.
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

package com.blazebit.expression.declarative.view;

import com.blazebit.domain.boot.model.MetadataDefinition;
import com.blazebit.domain.boot.model.MetadataDefinitionHolder;
import com.blazebit.expression.persistence.CorrelationRenderer;
import com.blazebit.expression.persistence.PersistenceExpressionSerializer;
import com.blazebit.persistence.CorrelationQueryBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FromProvider;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.metamodel.CorrelatedAttribute;

import javax.persistence.metamodel.EntityType;
import java.util.Collections;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
class CorrelationProviderCorrelationRendererImpl implements CorrelationRenderer, MetadataDefinition<CorrelationRenderer> {

    private final CorrelationProviderFactory correlationProviderFactory;
    private final String correlationBasis;
    private final String correlationResult;

    /**
     * Creates a new correlation renderer for a correlatedAttribute attribute.
     *
     * @param correlatedAttribute The correlated attribute
     */
    public CorrelationProviderCorrelationRendererImpl(CorrelatedAttribute<?, ?> correlatedAttribute) {
        this.correlationProviderFactory = correlatedAttribute.getCorrelationProviderFactory();
        this.correlationBasis = correlatedAttribute.getCorrelationBasis();
        this.correlationResult = correlatedAttribute.getCorrelationResult();
    }

    @Override
    public String correlate(CriteriaBuilder<?> cb, String parentAlias, PersistenceExpressionSerializer serializer) {
        DefaultViewRootJpqlMacro.registerIfAbsent(serializer, parentAlias);
        MutableEmbeddingViewJpqlMacro.withEmbeddingViewPath(serializer, parentAlias);
        CorrelationProvider correlationProvider = correlationProviderFactory.create(null, Collections.emptyMap());
        CorrelationBuilderImpl correlationBuilder = new CorrelationBuilderImpl(cb, serializer.nextCorrelationAlias());
        String correlationExpression;
        if (parentAlias == null) {
            correlationExpression = correlationBasis;
        } else {
            correlationExpression = parentAlias + "." + correlationBasis;
        }
        correlationProvider.applyCorrelation(correlationBuilder, correlationExpression);
        if (correlationResult == null) {
            return correlationBuilder.getCorrelationAlias();
        } else {
            return correlationBuilder.getCorrelationAlias() + "." + correlationResult;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<CorrelationRenderer> getJavaType() {
        return CorrelationRenderer.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CorrelationRenderer build(MetadataDefinitionHolder<?> definitionHolder) {
        return this;
    }

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    private static class CorrelationBuilderImpl implements CorrelationBuilder {

        private final CriteriaBuilder<?> criteriaBuilder;
        private final String alias;
        private boolean correlated;

        /**
         * Creates a new correlation builder.
         *
         * @param criteriaBuilder The criteria builder
         * @param alias The correlation alias
         */
        public CorrelationBuilderImpl(CriteriaBuilder<?> criteriaBuilder, String alias) {
            this.criteriaBuilder = criteriaBuilder;
            this.alias = alias;
        }

        @Override
        public <T> T getService(Class<T> serviceClass) {
            return criteriaBuilder.getService(serviceClass);
        }

        @Override
        public FromProvider getCorrelationFromProvider() {
            return criteriaBuilder;
        }

        @Override
        public String getCorrelationAlias() {
            return alias;
        }

        @Override
        public JoinOnBuilder<CorrelationQueryBuilder> correlate(Class<?> entityClass) {
            if (correlated) {
                throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
            }
            correlated = true;
            return (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(entityClass, alias);
        }

        @Override
        public JoinOnBuilder<CorrelationQueryBuilder> correlate(EntityType<?> entityType) {
            if (correlated) {
                throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
            }
            correlated = true;
            return (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(entityType, alias);
        }
    }

}
