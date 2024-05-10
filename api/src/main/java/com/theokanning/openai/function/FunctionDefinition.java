package com.theokanning.openai.function;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import lombok.Getter;
import lombok.NonNull;

import java.util.function.Function;

/**
 * @author LiangTao
 * @date 2024年05月10 11:15
 **/
@JsonSerialize(using = FunctionParametersSerializer.class)
@Getter
public class FunctionDefinition {

    /**
     * not allow to create instance by constructor
     */
    private FunctionDefinition() {

    }

    /**
     * The name of the function being called.
     */
    @NonNull
    protected String name;

    /**
     * A description of what the function does, used by the model to choose when and how to call the function.
     */
    private String description;

    /**
     * parameters definition by class schema ,will use {@link JsonSchemaGenerator} to generate json schema
     */
    private Class<?> parametersDefinitionClass;

    /**
     * The parameters the functions accepts.Choose between this parameter and {@link #parametersDefinitionClass}
     **/
    private Object parametersDefinition;

    /**
     * Function executor,if set {@link #parametersDefinitionClass},the executor type must {@link #parametersDefinitionClass}. <br>
     * Else executor parameter type must {@link #parametersDefinition} JsonNode type
     * 可以为null
     */
    private Function<Object, Object> executor;

    public static <T> FunctionDefinition.Builder<T> builder() {
        return new FunctionDefinition.Builder<>();
    }

    public static class Builder<T> {
        private String name;
        private String description;
        private Class<T> parametersDefinitionClass;

        private T parametersDefinition;

        private Function<T, Object> executor;

        public FunctionDefinition.Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public FunctionDefinition.Builder<T> description(String description) {
            this.description = description;
            return this;
        }

        public FunctionDefinition.Builder<T> parametersDefinition(T parametersDefinition) {
            this.parametersDefinition = parametersDefinition;
            return this;
        }

        public FunctionDefinition.Builder<T> parametersDefinitionByClass(Class<T> parametersDefinitionClass) {
            this.parametersDefinitionClass = parametersDefinitionClass;
            return this;
        }

        public FunctionDefinition.Builder<T> executor(Function<T, Object> executor) {
            this.executor = executor;
            return this;
        }


        @SuppressWarnings("unchecked")
        public FunctionDefinition build() {
            if (name == null) {
                throw new IllegalArgumentException("name can't be null");
            }
            if (parametersDefinitionClass == null && parametersDefinition == null) {
                throw new IllegalArgumentException("parametersDefinitionClass and parametersDefinition can't be null at the same time,please set one of them");
            }
            if (parametersDefinition != null && parametersDefinitionClass != null) {
                throw new IllegalArgumentException("parametersDefinitionClass and parametersDefinition can't be set at the same time,please set one of them");
            }
            FunctionDefinition functionDefinition = new FunctionDefinition();
            functionDefinition.name = name;
            functionDefinition.description = description;
            functionDefinition.parametersDefinitionClass = parametersDefinitionClass;
            functionDefinition.parametersDefinition = parametersDefinition;
            functionDefinition.executor = (Function<Object, Object>) executor;
            return functionDefinition;
        }
    }
}
