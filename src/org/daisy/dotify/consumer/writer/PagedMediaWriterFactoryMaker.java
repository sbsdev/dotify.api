package org.daisy.dotify.consumer.writer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactory;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryService;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * Provides a PagedMediaWriter factory maker. This is the entry point for
 * creating PagedMediaWriter instances.
 * 
 * @author Joel Håkansson
 */
@Component
public class PagedMediaWriterFactoryMaker implements
		PagedMediaWriterFactoryMakerService {
	private final List<PagedMediaWriterFactoryService> filters;
	private final Map<String, PagedMediaWriterFactoryService> map;
	private final Logger logger;

	/**
	 * Creates a new paged media writer factory maker.
	 */
	public PagedMediaWriterFactoryMaker() {
		logger = Logger.getLogger(this.getClass().getCanonicalName());
		filters = new CopyOnWriteArrayList<>();
		this.map = Collections.synchronizedMap(new HashMap<String, PagedMediaWriterFactoryService>());
	}
	
	/**
	 * <p>
	 * Creates a new PagedMediaWriterFactoryMaker and populates it using the SPI
	 * (java service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the PagedMediaWriterFactoryMakerService
	 * interface.
	 * </p>
	 * 
	 * @return returns a new PagedMediaWriterFactoryMaker
	 */
	public static PagedMediaWriterFactoryMaker newInstance() {
		PagedMediaWriterFactoryMaker ret = new PagedMediaWriterFactoryMaker();
		{
			Iterator<PagedMediaWriterFactoryService> i = ServiceLoader.load(PagedMediaWriterFactoryService.class).iterator();
			while (i.hasNext()) {
				PagedMediaWriterFactoryService factory = i.next();
				factory.setCreatedWithSPI();
				ret.addFactory(factory);
			}
		}
		return ret;
	}
	
	/**
	 * Adds a factory (intended for use by the OSGi framework)
	 * @param factory the factory to add
	 */
	@Reference(type = '*')
	public void addFactory(PagedMediaWriterFactoryService factory) {
		logger.finer("Adding factory: " + factory);
		filters.add(factory);
	}

	/**
	 * Removes a factory (intended for use by the OSGi framework)
	 * @param factory the factory to remove
	 */
	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(PagedMediaWriterFactoryService factory) {
		logger.finer("Removing factory: " + factory);
		// this is to avoid adding items to the cache that were removed while
		// iterating
		synchronized (map) {
			filters.remove(factory);
			map.clear();
		}
	}

	@Override
	public PagedMediaWriterFactory getFactory(String target) throws PagedMediaWriterConfigurationException {
		PagedMediaWriterFactoryService template = map.get(target.toLowerCase());
		if (template==null) {
			// this is to avoid adding items to the cache that were removed
			// while iterating
			synchronized (map) {
				for (PagedMediaWriterFactoryService h : filters) {
					if (h.supportsMediaType(target)) {
						logger.fine("Found a PagedMediaWriter factory for " + target + " (" + h.getClass() + ")");
						map.put(target.toLowerCase(), h);
						template = h;
						break;
					}
				}
			}
		}
		if (template==null) {
			throw new PagedMediaWriterFactoryConfigurationException("Cannot find a PagedMediaWriter factory for " + target);
		}
		return template.newFactory(target);
	}

	@Override
	public PagedMediaWriter newPagedMediaWriter(String target) throws PagedMediaWriterConfigurationException {
		return getFactory(target).newPagedMediaWriter();
	}
	
	private class PagedMediaWriterFactoryConfigurationException extends PagedMediaWriterConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1296708210640963472L;

		PagedMediaWriterFactoryConfigurationException(String message) {
			super(message);
		}
		
	}

	@Override
	public Collection<String> listMediaTypes() {
		ArrayList<String> ret = new ArrayList<>();
		for (PagedMediaWriterFactoryService s : filters) {
			ret.addAll(s.listMediaTypes());
		}
		return ret;
	}

}
