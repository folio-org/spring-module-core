/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * This file is a copy and psate of the Spring StandardRandomStrategy with minor changes to the class names.
 * Any deprecated annotation or comments are also removed.
 * A serialVersionUID is added.
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.folio.spring.domain.generator;

import java.util.UUID;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
* Implements a "random" UUID generation strategy as defined by the {@link UUID#randomUUID()} method.
*
* @author Steve Ebersole
*/
public class SpringStandardRandomStrategy implements SpringUUIDGenerationStrategy {

  private static final long serialVersionUID = -876175072650422030L;

  public static final SpringStandardRandomStrategy INSTANCE = new SpringStandardRandomStrategy();

  /**
   * A variant 4 (random) strategy
   */
  public int getGeneratedVersion() {
    // a "random" strategy
    return 4;
  }

  /**
   * Delegates to {@link UUID#randomUUID()}
   */
  public UUID generateUUID(SharedSessionContractImplementor session) {
    return generateUuid( session );
  }

  public UUID generateUuid(SharedSessionContractImplementor session) {
    return UUID.randomUUID();
  }
}
