
package org.spiderflow.core.expression.interpreter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.spiderflow.core.expression.Error.TemplateException;
import org.spiderflow.core.expression.Template;
import org.spiderflow.core.expression.TemplateContext;
import org.spiderflow.core.expression.parsing.Ast;
import org.spiderflow.core.expression.parsing.Ast.Break;
import org.spiderflow.core.expression.parsing.Ast.Continue;
import org.spiderflow.core.expression.parsing.Ast.Node;
import org.spiderflow.core.expression.parsing.Ast.Return;
import org.spiderflow.core.expression.parsing.Ast.Return.ReturnValue;

/**
 * <p>
 * Interprets a Template given a TemplateContext to lookup variable values in and writes the evaluation results to an output
 * stream. Uses the global {@link Reflection} instance as returned by {@link Reflection#getInstance()} to access members and call
 * methods.
 * </p>
 *
 * <p>
 * The interpeter traverses the AST as stored in {@link Template#getNodes()}. the interpeter has a method for each AST node type
 * (see {@link Ast} that evaluates that node. A node may return a value, to be used in the interpretation of a parent node or to
 * be written to the output stream.
 * </p>
 **/
public class AstInterpreter {
	public static Object interpret (Template template, TemplateContext context, OutputStream out) {
		try {
			Object result = interpretNodeList(template.getNodes(), template, context, out);
			if (result == Return.RETURN_SENTINEL) {
				return ((ReturnValue)result).getValue();
			} else {
				return null;
			}
		} catch (Throwable t) {
			if (t instanceof TemplateException)
				throw (TemplateException)t;
			else {
				org.spiderflow.core.expression.Error.error("Couldn't interpret node list due to I/O error, " + t.getMessage(), template.getNodes().get(0).getSpan());
				return null; // never reached
			}
		} finally {
			// clear out RETURN_SENTINEL as it uses a ThreadLocal and would leak memory otherwise
			Return.RETURN_SENTINEL.setValue(null);
		}
	}

	public static Object interpretNodeList (List<Node> nodes, Template template, TemplateContext context, OutputStream out) throws IOException {
		for (int i = 0, n = nodes.size(); i < n; i++) {
			Node node = nodes.get(i);
			Object value = node.evaluate(template, context, out);
			if (value != null) {
				if (value == Break.BREAK_SENTINEL || value == Continue.CONTINUE_SENTINEL || value == Return.RETURN_SENTINEL)
					return value;
				else
					out.write(value.toString().getBytes("UTF-8"));
			}
		}
		return null;
	}
}
