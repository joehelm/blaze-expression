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

package com.blazebit.expression.persistence;

import com.blazebit.domain.runtime.model.DomainOperator;
import com.blazebit.domain.runtime.model.DomainType;
import com.blazebit.domain.runtime.model.TemporalInterval;
import com.blazebit.expression.ChainingArithmeticExpression;
import com.blazebit.expression.ComparisonOperator;
import com.blazebit.expression.Expression;
import com.blazebit.expression.Literal;
import com.blazebit.expression.spi.ComparisonOperatorInterpreter;
import com.blazebit.expression.spi.DomainOperatorInterpreter;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class TimeOperatorHandler implements ComparisonOperatorInterpreter, DomainOperatorInterpreter, DomainOperatorRenderer, Serializable {

    public static final TimeOperatorHandler INSTANCE = new TimeOperatorHandler();

    private TimeOperatorHandler() {
    }

    @Override
    public Boolean interpret(DomainType leftType, DomainType rightType, Object leftValue, Object rightValue, ComparisonOperator operator) {
        if (leftValue instanceof LocalTime && rightValue instanceof LocalTime) {
            LocalTime l = (LocalTime) leftValue;
            LocalTime r = (LocalTime) rightValue;
            switch (operator) {
                case EQUAL:
                    return l.compareTo(r) == 0;
                case NOT_EQUAL:
                    return l.compareTo(r) != 0;
                case GREATER_OR_EQUAL:
                    return l.compareTo(r) > -1;
                case GREATER:
                    return l.compareTo(r) > 0;
                case LOWER_OR_EQUAL:
                    return l.compareTo(r) < 1;
                case LOWER:
                    return l.compareTo(r) < 0;
                default:
                    break;
            }
        } else {
            throw new IllegalArgumentException("Illegal arguments [" + leftValue + ", " + rightValue + "]!");
        }

        throw new IllegalArgumentException("Can't handle the operator " + operator + " for the arguments [" + leftValue + ", " + rightValue + "]!");
    }

    @Override
    public Object interpret(DomainType targetType, DomainType leftType, DomainType rightType, Object leftValue, Object rightValue, DomainOperator operator) {
        if (leftValue instanceof TemporalInterval && rightValue instanceof TemporalInterval) {
            TemporalInterval interval1 = (TemporalInterval) leftValue;
            TemporalInterval interval2 = (TemporalInterval) rightValue;

            switch (operator) {
                case PLUS:
                    return interval1.add(interval2);
                case MINUS:
                    return interval1.subtract(interval2);
                default:
                    break;
            }
        } else if (leftValue instanceof LocalTime && rightValue instanceof TemporalInterval) {
            LocalTime localTime = (LocalTime) leftValue;
            TemporalInterval interval = (TemporalInterval) rightValue;
            switch (operator) {
                case PLUS:
                    return interval.add(localTime);
                case MINUS:
                    return interval.subtract(localTime);
                default:
                    break;
            }
        } else if (leftValue instanceof TemporalInterval && rightValue instanceof LocalTime) {
            TemporalInterval interval = (TemporalInterval) leftValue;
            LocalTime localTime = (LocalTime) rightValue;

            if (operator == DomainOperator.PLUS) {
                return interval.add(localTime);
            }
        } else {
            throw new IllegalArgumentException("Illegal arguments [" + leftValue + ", " + rightValue + "]!");
        }

        throw new IllegalArgumentException("Can't handle the operator " + operator + " for the arguments [" + leftValue + ", " + rightValue + "]!");
    }

    @Override
    public void render(ChainingArithmeticExpression e, PersistenceExpressionSerializer serializer) {
        DomainOperator domainOperator = e.getOperator().getDomainOperator();
        if (domainOperator == DomainOperator.PLUS || domainOperator == DomainOperator.MINUS) {
            int factor = domainOperator == DomainOperator.PLUS ? 1 : -1;
            Expression expression = null;
            TemporalInterval interval = null;
            StringBuilder sb = serializer.getStringBuilder();
            if (e.getLeft() instanceof Literal) {
                if (domainOperator == DomainOperator.PLUS) {
                    expression = e.getRight();
                    interval = (TemporalInterval) ((Literal) e.getLeft()).getValue();
                }
            } else if (e.getRight() instanceof Literal) {
                expression = e.getLeft();
                interval = (TemporalInterval) ((Literal) e.getRight()).getValue();
            }

            if (interval != null) {
                int seconds = 0;
                if (interval.getHours() != 0) {
                    seconds = interval.getHours() * 60 * 60;
                }
                if (interval.getMinutes() != 0) {
                    seconds += interval.getMinutes() * 60;
                }
                if (interval.getSeconds() != 0) {
                    seconds += interval.getSeconds();
                }
                if (seconds != 0) {
                    sb.append("ADD_SECOND(");
                }
                expression.accept(serializer);
                if (seconds != 0) {
                    sb.append(", ");
                    sb.append(seconds * factor).append(')');
                }
            }
        }
        throw new IllegalArgumentException("Can't handle the operator " + domainOperator + " for the arguments [" + e.getLeft() + ", " + e.getRight() + "]!");
    }
}
