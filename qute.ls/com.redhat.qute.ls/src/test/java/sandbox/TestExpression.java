package sandbox;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.qute.Engine;
import io.quarkus.qute.ReflectionValueResolver;
import io.quarkus.qute.Template;

public class TestExpression {

	public static void main(String[] args) {
		Engine engine = Engine.builder().addDefaults().addValueResolver(new ReflectionValueResolver()).build();
		Template template = engine.parse("{foo ? 456 : 123}");
		
		Map<String, Object> data = new HashMap<>();
		data.put("foo", "true");
		String s = template.data(data).render();
		System.err.println(s);
	}
}
