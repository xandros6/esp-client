package org.esp.server;

import it.jrc.auth.AuthServlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.expressme.openid.OpenIdManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import freemarker.template.Configuration;

@Singleton
public class FakeAuthServlet extends AuthServlet {

    @Inject
    public FakeAuthServlet(OpenIdManager manager, 
            @Named("context_path") String contextPath,
            Configuration templateConf, EntityManagerFactory emf) {
        super(manager, contextPath, templateConf, emf);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {


            SecurityUtils.getSubject().login(new UsernamePasswordToken("willtemperley@gmail.com", ""));
            super.doGet(request, response);

    }

}
