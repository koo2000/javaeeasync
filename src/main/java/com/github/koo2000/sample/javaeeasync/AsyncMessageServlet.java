package com.github.koo2000.sample.javaeeasync;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsyncMessageServlet extends HttpServlet {

    private static final String ASYNC_ID_KEY = "ASYNC_ID";

    private static final Map<String, ArrayList<AsyncContext>> CONTEXTES = new HashMap<>();
    private static MyContextListener MYLISTENER = new MyContextListener();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.getRequestDispatcher("WEB-INF/jsp/async-request.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String id = req.getParameter("id");
        String message = req.getParameter("message");
        sendMessage(id, message);
        req.getRequestDispatcher("WEB-INF/jsp/async-request.jsp").forward(req, res);
    }

    private static void sendMessage(String id, String message) throws IOException {
        if (CONTEXTES.containsKey(id)) {
            List<AsyncContext> copy = (List<AsyncContext>) CONTEXTES.get(id).clone();
            for (AsyncContext context :
                    copy) {
                context.getResponse().getWriter().println(message);
                context.getResponse().getWriter().println();
                context.getResponse().getWriter().flush();

                System.out.println("send message");
                context.complete();
            }
        }
    }

    public static synchronized void register(String id, AsyncContext context) throws IOException {
        if (!CONTEXTES.containsKey(id)) {
            CONTEXTES.put(id, new ArrayList<AsyncContext>());
        }

        context.getRequest().setAttribute(ASYNC_ID_KEY, id);
        CONTEXTES.get(id).add(context);

        context.addListener(new MyContextListener());
        context.setTimeout(120 * 1000);
        context.getResponse().getWriter().println("start async with id:" + id);
        context.getResponse().getWriter().flush();
        System.out.println("start async");
    }

    private static class MyContextListener implements AsyncListener {


        @Override
        public void onComplete(AsyncEvent asyncEvent) throws IOException {
            removeContext(asyncEvent);
            System.out.println("onComplete");
        }

        @Override
        public void onTimeout(AsyncEvent asyncEvent) throws IOException {
            removeContext(asyncEvent);
            System.out.println("onTimeout");
        }

        @Override
        public void onError(AsyncEvent asyncEvent) throws IOException {
            removeContext(asyncEvent);
            System.out.println("onError");
        }

        @Override
        public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
            System.out.println("onStartAsync");
        }
    }

    private static synchronized void removeContext(AsyncEvent asyncEvent) {
        String id = (String) asyncEvent.getAsyncContext().getRequest().getAttribute(ASYNC_ID_KEY);
        CONTEXTES.get(id).remove(asyncEvent.getAsyncContext());
    }
}
