package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.guice.ModuleFactory;
import com.epam.dlab.backendapi.resources.DockerResource;
import com.epam.dlab.backendapi.resources.LoginResource;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * Created by Alexey Suprun
 */
public class SelfServiceApplication extends Application<SelfServiceApplicationConfiguration> {
    public static void main(String... args) throws Exception {
        new SelfServiceApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<SelfServiceApplicationConfiguration> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.addBundle(new AssetsBundle("/webapp/", "/webapp"));
//        bootstrap.addBundle(new ViewBundle<SelfServiceApplicationConfiguration>());
    }

    @Override
    public void run(SelfServiceApplicationConfiguration configuration, Environment environment) throws Exception {
        Injector injector = Guice.createInjector(ModuleFactory.getModule(configuration, environment));
        environment.jersey().register(MultiPartFeature.class);
        environment.jersey().register(injector.getInstance(LoginResource.class));
        environment.jersey().register(injector.getInstance(DockerResource.class));
//        environment.jersey().register(injector.getInstance(AfterLoginResource.class));
//        environment.jersey().register(injector.getInstance(LoginResource.class));
//        environment.jersey().register(injector.getInstance(LogoutResource.class));

//        RestAuthenticator authenticator = injector.getInstance(RestAuthenticator.class);
//        environment.jersey().register(new AuthDynamicFeature(
//                new OAuthCredentialAuthFilter.Builder<UserInfo>()
//                        .setAuthenticator( authenticator )
//                        .setAuthorizer(new Authorizer<UserInfo>(){
//                            @Override
//                            public boolean authorize(UserInfo principal, String role) {
//                                TODO: Replace this code when need real roles support. This left here as example
//                                return true;
//                            }})
//                        .setPrefix("Bearer")
//                        .setUnauthorizedHandler(new RestAuthFailureHandler(configuration.getAuthenticationServiceConfiguration()))
//                        .buildAuthFilter()));
//
//        environment.jersey().register(RolesAllowedDynamicFeature.class);
//        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(UserInfo.class));
    }
}
