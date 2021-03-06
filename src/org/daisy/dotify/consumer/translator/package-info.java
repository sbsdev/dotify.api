/**
 * Provides braille translation consumers. This package is only visible in
 * a non-OSGi environment. In an OSGi context, classes in this 
 * package must be retrieved from the service registry under the interfaces
 * they implement.
 * 
 * <p>
 * The entry point for translating braille is the BrailleTranslatorFactoryMaker
 * where an instance of a BrailleTranslator can be obtained.
 * </p>
 * 
 * <p>
 * To add a translator for another language:
 * </p>
 * <ul>
 * <li>Implement BrailleTranslator and BrailleTranslatorFactory for your locale
 * and place it the org.daisy.dotify.impl.translator package or in a sub package
 * thereof.</li>
 * <li>Add the name of your implementation to the
 * org.daisy.dotify.api.translator.BrailleTranslatorFactory file in
 * META-INF/services</li>
 * <li>If your implementation uses a grade not defined in
 * BrailleTranslatorFactory, please add it to the API so that others may know
 * about it.</li>
 * </ul>
 * 
 * @author Joel Håkansson
 */
package org.daisy.dotify.consumer.translator;