/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.persistence.lifecycle;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.persistence.configuration.PersistenceConfiguration;
import org.jboss.arquillian.persistence.event.AfterPersistenceTest;
import org.jboss.arquillian.persistence.event.BeforePersistenceTest;
import org.jboss.arquillian.persistence.event.CleanUpData;
import org.jboss.arquillian.persistence.event.CompareData;
import org.jboss.arquillian.persistence.event.PrepareData;
import org.jboss.arquillian.persistence.metadata.DataSetProvider;
import org.jboss.arquillian.persistence.metadata.ExpectedDataSetProvider;
import org.jboss.arquillian.persistence.metadata.MetadataExtractor;
import org.jboss.arquillian.persistence.metadata.MetadataProvider;

public class DatasetHandler
{

   @Inject
   private Instance<MetadataExtractor> metadataExtractor;

   @Inject
   private Instance<MetadataProvider> metadataProvider;

   @Inject
   private Instance<PersistenceConfiguration> configuration;

   @Inject
   private Event<PrepareData> prepareDataEvent;

   @Inject
   private Event<CompareData> compareDataEvent;

   @Inject
   private Event<CleanUpData> cleanUpDataEvent;

   public void seedDatabase(@Observes(precedence = 20) BeforePersistenceTest beforePersistenceTest)
   {
      if (metadataProvider.get().isDataSeedOperationRequested())
      {
         DataSetProvider dataSetProvider = new DataSetProvider(metadataExtractor.get(), configuration.get());
         prepareDataEvent.fire(new PrepareData(beforePersistenceTest, dataSetProvider.getDescriptors(beforePersistenceTest.getTestMethod())));
      }

   }

   public void verifyDatabase(@Observes(precedence = 30) AfterPersistenceTest afterPersistenceTest)
   {
      if (metadataProvider.get().isDataVerificationRequested())
      {
         ExpectedDataSetProvider dataSetProvider = new ExpectedDataSetProvider(metadataExtractor.get(), configuration.get());
         compareDataEvent.fire(new CompareData(afterPersistenceTest, dataSetProvider.getDescriptors(afterPersistenceTest.getTestMethod())));
      }

      cleanUpDataEvent.fire(new CleanUpData(afterPersistenceTest));
   }

}
