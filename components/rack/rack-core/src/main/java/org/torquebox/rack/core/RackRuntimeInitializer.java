/* Copyright 2010 Red Hat, Inc. */
package org.torquebox.rack.core;

import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.torquebox.interp.spi.RuntimeInitializer;
import org.torquebox.rack.metadata.RackApplicationMetaData;


/**
 * {@link RuntimeInitializer} for Ruby Rack applications.
 *
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RackRuntimeInitializer implements RuntimeInitializer {

    private static final Logger log = Logger.getLogger( RackRuntimeInitializer.class );

    public RackRuntimeInitializer(RackApplicationMetaData rackMetaData) {
        this.rackMetaData = rackMetaData;
    }

    @Override
    public void initialize(Ruby ruby) throws Exception {
        ruby.evalScriptlet(getInitializerScript());
        ruby.setCurrentDirectory(this.rackMetaData.getRackRoot().getPhysicalFile().getCanonicalPath());
        log.info("Current directory: "+ruby.getCurrentDirectory());

    }

    /** Create the initializer script.
     *
     * @return The initializer script.
     */
    protected String getInitializerScript() {
        StringBuilder script = new StringBuilder();
        String rackRootPath = this.rackMetaData.getRackRootPath();
        String rackEnv = this.rackMetaData.getRackEnv();
        String contextPath = this.rackMetaData.getContextPath();

        if (rackRootPath.endsWith("/")) {
            rackRootPath = rackRootPath.substring(0, rackRootPath.length() - 1);
        }

        if (! rackRootPath.startsWith("vfs:/")) {
            if (!rackRootPath.startsWith("/")) {
                rackRootPath = "/" + rackRootPath;
            }
        }

        script.append("RACK_ROOT=%q(" + rackRootPath + ")\n");
        script.append("RACK_ENV=%q(" + rackEnv + ")\n");
        script.append("ENV['RACK_ROOT']=%q(" + rackRootPath + ")\n");
        script.append("ENV['RACK_ENV']=%q(" + rackEnv + ")\n");

        if ( contextPath != null && contextPath.length() > 1 ) { // only set if not root context
            script.append("ENV['RAILS_RELATIVE_URL_ROOT']=%q(" + contextPath + ")\n");
            script.append("ENV['RACK_BASE_URI']=%q(" + contextPath + ")\n");
        }

        return script.toString();
    }

    private RackApplicationMetaData rackMetaData;

}
