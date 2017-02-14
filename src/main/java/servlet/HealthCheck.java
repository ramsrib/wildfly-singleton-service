package servlet;

import ejb.MyEJBService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Health Check Servlet
 **/
@WebServlet(urlPatterns = HealthCheck.SERVLET_PATH)
public class HealthCheck extends HttpServlet {

    private static final String SERVLET_NAME = "health";
    static final String SERVLET_PATH = "/" + SERVLET_NAME;

    @EJB
    MyEJBService ejbService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        boolean active = ejbService.isActive();
        boolean reallyActive = ejbService.isReallyActive();

        this.log(String.format("Health Check, node status: %s, expecting: %s.", active, reallyActive));

        resp.getWriter().write("Health Status:\n");
        resp.getWriter().write("Singleton EJB Service - Node Status: " + active + "\n");
        resp.getWriter().write("Singleton MSC Service - Node Status: " + reallyActive + "\n");

        if (active && reallyActive) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}