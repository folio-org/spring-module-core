package org.folio.spring.domain.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.folio.spring.domain.generator.CustomUUIDGenerator;
import org.hibernate.annotations.IdGeneratorType;

@IdGeneratorType(CustomUUIDGenerator.class)
@Retention(RUNTIME)
@Target({ METHOD, FIELD })
public @interface UUIDGenerator {
}
