package br.com.facilita.apuracao.api;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;

import javax.validation.constraints.NotBlank;

import org.junit.jupiter.api.Test;

class RequestConstraintsTest {

    @Test
    void requestsNaoUsamNotBlankDoBeanValidation2() {
        Class<?>[] types = new Class<?>[] {
                PrepararAnexoRequest.class,
                ConcluirAnexoRequest.class,
                AnexarRequest.class,
                VersionedApuracaoRequest.class
        };
        for (Class<?> type : types) {
            for (Field field : type.getDeclaredFields()) {
                assertFalse(field.isAnnotationPresent(NotBlank.class),
                        type.getSimpleName() + "." + field.getName()
                                + " usa NotBlank, que o validador do Om nao reconhece");
            }
        }
    }
}
