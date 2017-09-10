/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 *
 */

package io.javalin;

import org.junit.jupiter.api.Test;

import com.mashape.unirest.http.HttpMethod;

import static io.javalin.ApiBuilder.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestApiBuilder extends _UnirestBaseTest {

    @Test
    public void autoPrefix_path_works() throws Exception {
        app.routes(() -> {
            path("level-1", () -> {
                get("/hello", simpleAnswer("Hello from level 1"));
            });
        });
        assertThat(GET_body("/level-1/hello"), is("Hello from level 1"));
    }

    @Test
    public void routesWithoutPathArg_works() throws Exception {
        app.routes(() -> {
            path("api", () -> {
                get(OK_HANDLER);
                post(OK_HANDLER);
                put(OK_HANDLER);
                delete(OK_HANDLER);
                patch(OK_HANDLER);
                path("user", () -> {
                    get(OK_HANDLER);
                    post(OK_HANDLER);
                    put(OK_HANDLER);
                    delete(OK_HANDLER);
                    patch(OK_HANDLER);
                });
            });
        });
        HttpMethod[] httpMethods = new HttpMethod[] {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH};
        for (HttpMethod httpMethod : httpMethods) {
            assertThat(call(httpMethod, "/api").getStatus(), is(200));
            assertThat(call(httpMethod, "/api/user").getStatus(), is(200));
        }
    }

    @Test
    public void test_pathWorks_forGet() throws Exception {
        app.routes(() -> {
            get("/hello", simpleAnswer("Hello from level 0"));
            path("/level-1", () -> {
                get("/hello", simpleAnswer("Hello from level 1"));
                get("/hello-2", simpleAnswer("Hello again from level 1"));
                post("/create-1", simpleAnswer("Created something at level 1"));
                path("/level-2", () -> {
                    get("/hello", simpleAnswer("Hello from level 2"));
                    path("/level-3", () -> {
                        get("/hello", simpleAnswer("Hello from level 3"));
                    });
                });
            });
        });
        assertThat(GET_body("/hello"), is("Hello from level 0"));
        assertThat(GET_body("/level-1/hello"), is("Hello from level 1"));
        assertThat(GET_body("/level-1/level-2/hello"), is("Hello from level 2"));
        assertThat(GET_body("/level-1/level-2/level-3/hello"), is("Hello from level 3"));
    }

    private Handler simpleAnswer(String body) {
        return ctx -> ctx.result(body);
    }

    @Test
    public void test_pathWorks_forFilters() throws Exception {
        app.routes(() -> {
            path("/level-1", () -> {
                before("/*", ctx -> ctx.result("1"));
                path("/level-2", () -> {
                    path("/level-3", () -> {
                        get("/hello", updateAnswer("Hello"));
                    });
                    after("/*", updateAnswer("2"));
                });
            });
        });
        assertThat(GET_body("/level-1/level-2/level-3/hello"), is("1Hello2"));
    }

    private Handler updateAnswer(String body) {
        return ctx -> ctx.result(ctx.resultString() + body);
    }

    public void test_throwsException_ifUsedWithoutRoutes() throws Exception {
        assertThrows(IllegalStateException.class, () -> get("/null-static", ctx -> ctx.result("Shouldn't work")));
    }

}

