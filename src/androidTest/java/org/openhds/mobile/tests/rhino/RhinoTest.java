package org.openhds.mobile.tests.rhino;

import android.test.ActivityTestCase;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.openhds.mobile.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class RhinoTest extends ActivityTestCase {

    public void testSimpleEval() throws Exception {
        String script = "function square (x) { return x * x; } var answer = square(7);";
        Double expected = Double.valueOf(49);
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        try {
            Scriptable scope = ctx.initSafeStandardObjects();
            ctx.evaluateString(scope, script, getName(), 1, null);
            assertEquals(expected, scope.get("answer", null));
        } finally {
            Context.exit();
        }
    }

    public void testFileEval() throws IOException {
        InputStream scriptStream = getInstrumentation().getContext().getAssets().open("square.js");
        Reader scriptReader = new InputStreamReader(scriptStream);
        Double expected = Double.valueOf(81);
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        try {
            Scriptable scope = ctx.initSafeStandardObjects();
            ctx.evaluateReader(scope, scriptReader, getName(), 1, null);
            assertEquals(expected, scope.get("answer", null));
        } finally {
            Context.exit();
        }
    }

    public void testStaticMethodUsage() throws IOException {
        InputStream scriptStream = getInstrumentation().getContext().getAssets().open("square-using-java.js");
        Reader scriptReader = new InputStreamReader(scriptStream);
        Double expected = Double.valueOf(16);
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        try {
            Scriptable scope = ctx.initStandardObjects();
            ctx.evaluateReader(scope, scriptReader, getName(), 1, null);
            assertEquals(expected, scope.get("answer", null));
        } finally {
            Context.exit();
        }
    }

    public static class Math {
        public double square(double x) {
            return x * x;
        }
    }

    public void testObjectMethodUsage() throws IOException, NoSuchMethodException {
        InputStream scriptStream = getInstrumentation().getContext().getAssets().open("square-using-java-object.js");
        Reader scriptReader = new InputStreamReader(scriptStream);
        Double expected = Double.valueOf(25);
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        try {
            Scriptable scope = ctx.initStandardObjects();
            Object math = Context.javaToJS(new Math(), scope);
            ScriptableObject.putProperty(scope, "math", math);
            ctx.evaluateReader(scope, scriptReader, getName(), 1, null);
            assertEquals(expected, scope.get("answer", null));
        } finally {
            Context.exit();
        }
    }

    public interface Adder {
        int add(int x, int y);
    }

    public void testImplementJavaInterface() {
        String script = "var adder = new org.openhds.mobile.tests.rhino.RhinoTest.Adder({ add: function(x, y) { return x + y } });";
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        try {
            Scriptable scope = ctx.initStandardObjects();
            ctx.evaluateString(scope, script, getName(), 1, null);
            Adder adder = (Adder) Context.jsToJava(scope.get("adder", null), Adder.class);
            assertEquals(4, adder.add(1, 3));
        } finally {
            Context.exit();
        }
    }

    public void testCompiledScript() {
        String script = "var adder = new org.openhds.mobile.tests.rhino.RhinoTest.Adder({ add: function(x, y) { return x + y } });";
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        Scriptable scope;
        Script compiled;
        try {
            scope = ctx.initStandardObjects();
            compiled = ctx.compileString(script, getName(), 1, null);
        } finally {
            Context.exit();
        }

        ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        try {
            compiled.exec(ctx, scope);
            Adder adder = (Adder) Context.jsToJava(scope.get("adder", null), Adder.class);
            assertEquals(4, adder.add(1, 3));
        } finally {
            Context.exit();
        }
    }

    public void testGeneratedObjectUsableWithoutContext() {
        String script = "var adder = new org.openhds.mobile.tests.rhino.RhinoTest.Adder({ add: function(x, y) { return x + y } });";
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        Adder adder;
        try {
            Scriptable scope = ctx.initStandardObjects();
            ctx.evaluateString(scope, script, getName(), 1, null);
            adder = (Adder) Context.jsToJava(scope.get("adder", null), Adder.class);
        } finally {
            Context.exit();
        }
        assertEquals(4, adder.add(1, 3));
    }
}
