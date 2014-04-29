package org.commonjava.maven.enforcer.rule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Profile;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

public class EnforceManagedDepsRule
    implements EnforcerRule
{
    private boolean checkProfiles = true;

    private boolean failOnViolation = true;

    @Override
    public void execute( final EnforcerRuleHelper helper )
        throws EnforcerRuleException
    {
        final Log log = helper.getLog();

        try
        {
            // get the various expressions out of the helper.
            final MavenProject project = (MavenProject) helper.evaluate( "${project}" );
            final Model model = project.getOriginalModel();

            final Set<Dependency> failed = new HashSet<Dependency>();

            log.debug("Checking model...");

            check( model, failed );

            if ( checkProfiles )
            {
                log.debug("Checking profiles...");
                final List<Profile> profiles = project.getModel().getProfiles();
                if ( profiles != null && !profiles.isEmpty() )
                {
                    for ( final Profile profile : profiles )
                    {
                        check( profile, failed );
                    }
                }
            }

            final String message = buildFailureMessage( failed );

            if ( message != null )
            {
                if ( this.failOnViolation )
                {
                    throw new EnforcerRuleException( message );
                }
                else
                {
                    log.warn( message );
                }
            }
        }
        catch ( final ExpressionEvaluationException e )
        {
            throw new EnforcerRuleException( "Unable to lookup an expression " + e.getLocalizedMessage(), e );
        }
    }

    private String buildFailureMessage( final Set<Dependency> failed )
    {
        if ( failed == null || failed.isEmpty() )
        {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append( "The following " )
          .append( failed.size() )
          .append( " dependencies are NOT using a managed version:\n" );

        for ( final Dependency d : failed )
        {
            sb.append( "\n  - " )
              .append( d.getManagementKey() );
        }

        return sb.toString();
    }

    private void check( final ModelBase src, final Set<Dependency> failed )
    {
        final List<Dependency> dependencies = src.getDependencies();
        if ( dependencies != null && !dependencies.isEmpty() )
        {
            for ( final Dependency dependency : dependencies )
            {
                if ( dependency.getVersion() != null )
                {
                    failed.add( dependency );
                }
            }
        }
    }

    /**
     * If your rule is cacheable, you must return a unique id when parameters or conditions
     * change that would cause the result to be different. Multiple cached results are stored
     * based on their id.
     *
     * The easiest way to do this is to return a hash computed from the values of your parameters.
     *
     * If your rule is not cacheable, then the result here is not important, you may return anything.
     */
    @Override
    public String getCacheId()
    {
        //no hash on boolean...only parameter so no hash is needed.
        return "not-cached";
    }

    /**
     * This tells the system if the results are cacheable at all. Keep in mind that during
     * forked builds and other things, a given rule may be executed more than once for the same
     * project. This means that even things that change from project to project may still
     * be cacheable in certain instances.
     */
    @Override
    public boolean isCacheable()
    {
        return false;
    }

    /**
     * If the rule is cacheable and the same id is found in the cache, the stored results
     * are passed to this method to allow double checking of the results. Most of the time
     * this can be done by generating unique ids, but sometimes the results of objects returned
     * by the helper need to be queried. You may for example, store certain objects in your rule
     * and then query them later.
     */
    @Override
    public boolean isResultValid( final EnforcerRule rule )
    {
        return false;
    }

    public boolean isCheckProfiles()
    {
        return checkProfiles;
    }

    public void setCheckProfiles( final boolean checkProfiles )
    {
        this.checkProfiles = checkProfiles;
    }

    public boolean isFailOnViolation()
    {
        return failOnViolation;
    }

    public void setFailOnViolation( final boolean failOnViolation )
    {
        this.failOnViolation = failOnViolation;
    }
}
