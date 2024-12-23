/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * This file is a copy and psate of the Spring StandardRandomStrategy with minor changes to the class names.
 * Any deprecated annotation or comments are also removed.
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.folio.spring.domain.generator;

import java.util.Properties;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.spi.StandardGenerator;
import org.hibernate.id.uuid.StandardRandomStrategy;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.UUIDJavaType;

/**
 * An {@link IdentifierGenerator} which generates {@link UUID} values using a pluggable
 * {@link SpringUUIDGenerationStrategy generation strategy}.  The values this generator can return
 * include {@link UUID}, {@link String} and byte[16]
 * <p>
 * Supports 2 config parameters:<ul>
 * <li>{@link #UUID_GEN_STRATEGY} - names the {@link SpringUUIDGenerationStrategy} instance to use</li>
 * <li>{@link #UUID_GEN_STRATEGY_CLASS} - names the {@link SpringUUIDGenerationStrategy} class to use</li>
 * </ul>
 * <p>
 * Currently, there are 2 standard implementations of {@link SpringUUIDGenerationStrategy}:<ul>
 * <li>{@link StandardRandomStrategy} (the default, if none specified)</li>
 * <li>{@link org.hibernate.id.uuid.CustomVersionOneStrategy}</li>
 * </ul>
 */
public class SpringUUIDGenerator implements IdentifierGenerator, StandardGenerator {
  private static final CoreMessageLogger LOG = CoreLogging.messageLogger( SpringUUIDGenerator.class );

  public static final String UUID_GEN_STRATEGY = "uuid_gen_strategy";
  public static final String UUID_GEN_STRATEGY_CLASS = "uuid_gen_strategy_class";

  private SpringUUIDGenerationStrategy strategy;
  private UUIDJavaType.ValueTransformer valueTransformer;

  @Override
  public void configure(Type type, Properties parameters, ServiceRegistry serviceRegistry) throws MappingException {
    // check first for an explicit strategy instance
    strategy = (SpringUUIDGenerationStrategy) parameters.get( UUID_GEN_STRATEGY );

    if ( strategy == null ) {
      // next check for an explicit strategy class
      final String strategyClassName = parameters.getProperty( UUID_GEN_STRATEGY_CLASS );
      if ( strategyClassName != null ) {
        try {
          final ClassLoaderService cls = serviceRegistry.getService( ClassLoaderService.class );
          final Class<?> strategyClass = cls.classForName( strategyClassName );
          try {
            strategy = (SpringUUIDGenerationStrategy) strategyClass.newInstance();
          }
          catch ( Exception e ) {
            LOG.unableToInstantiateUuidGenerationStrategy(e);
          }
        }
        catch ( ClassLoadingException ignore ) {
          LOG.unableToLocateUuidGenerationStrategy( strategyClassName );
        }
      }
    }

    if ( strategy == null ) {
      // lastly use the standard random generator
      strategy = SpringStandardRandomStrategy.INSTANCE;
    }

    if ( UUID.class.isAssignableFrom( type.getReturnedClass() ) ) {
      valueTransformer = UUIDJavaType.PassThroughTransformer.INSTANCE;
    }
    else if ( String.class.isAssignableFrom( type.getReturnedClass() ) ) {
      // todo (6.0) : allow for org.hibernate.type.descriptor.java.UUIDJavaType.NoDashesStringTransformer
      valueTransformer = UUIDJavaType.ToStringTransformer.INSTANCE;
    }
    else if ( byte[].class.isAssignableFrom( type.getReturnedClass() ) ) {
      valueTransformer = UUIDJavaType.ToBytesTransformer.INSTANCE;
    }
    else {
      throw new HibernateException( "Unanticipated return type [" + type.getReturnedClass().getName() + "] for UUID conversion" );
    }
  }

  public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
    return valueTransformer.transform( strategy.generateUUID( session ) );
  }
}
