package com.github.koo2000.sample.javaeeasync;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AsyncReadServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String id = req.getParameter("id");
        req.startAsync();
        AsyncContext asyncContext = req.getAsyncContext();

        AsyncMessageServlet.register(id, asyncContext);
    }
}
