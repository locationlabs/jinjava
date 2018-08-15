
package com.hubspot.jinjava.lib.filter;

    import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Filters a sequence of objects by applying a test to an" +
        " attribute of an object and only selecting first with the" +
        " test succeeding. This is similar to selectattr but is fast" +
        " if you just want the first item",
    params = {
        @JinjavaParam(value = "sequence", type = "sequence", desc = "Sequence to test"),
        @JinjavaParam(value = "attr", desc = "Attribute to test for and select items that contain it"),
        @JinjavaParam(value = "exp_test", type = "name of expression test", defaultValue = "truthy",
            desc = "Specify which expression test to run for making the selection")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This expression would select the first containing content.post_list_summary_featured_image",
            code = "{{ contents|selectattrfirst('post_list_summary_featured_image') }}")
    })
public class SelectAttrFirstFilter implements AdvancedFilter {

  @Override
  public String getName() {
    return "selectattrfirst";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {
    interpreter.startRender(getName());
    try {
      if (args.length == 0) {
        throw new InterpretException(getName() + " filter requires an attr to filter on", interpreter.getLineNumber());
      }

      if (!(args[0] instanceof String)) {
        throw new InterpretException(getName() + " filter requires the filter attr arg to be a string", interpreter.getLineNumber());
      }

      Object[] expArgs = new String[]{};

      String attr = (String) args[0];

      ExpTest expTest = interpreter.getContext().getExpTest("truthy");
      if (args.length > 1) {

        if (!(args[1] instanceof String)) {
          throw new InterpretException(getName() + " filter requires the expression test arg to be a string", interpreter
              .getLineNumber());
        }

        expTest = interpreter.getContext().getExpTest((String) args[1]);
        if (expTest == null) {
          throw new InterpretException("No expression test defined with name '" + args[1] + "'", interpreter.getLineNumber());
        }

        if (args.length > 2) {
          expArgs = Arrays.copyOfRange(args, 2, args.length);
        }
      }

      ForLoop loop = ObjectIterator.getLoop(var);
      while (loop.hasNext()) {
        Object val = loop.next();
        Object attrVal = interpreter.resolveProperty(val, attr);

        if (expTest.evaluate(attrVal, interpreter, expArgs)) {
          return val;
        }
      }
      return null;
    } finally {
      interpreter.endRender(getName(), ImmutableMap.of("attr", Arrays.toString(args), "kwargs", kwargs));
    }
  }
}