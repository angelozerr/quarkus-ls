package com.redhat.qute.services.inlinevalue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Range;

import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.QutePositionUtility;

public class InlineValueASTVistitor extends ASTVisitor {

	private final List<InlineVariable> variables;

	public InlineValueASTVistitor() {
		this.variables = new ArrayList<>();
	}

	@Override
	public boolean visit(Expression node) {
		List<Node> content = node.getExpressionContent();

		for (Node child : content) {
			if (child.getKind() == NodeKind.ExpressionParts) {
				int start = -1;
				int end = -1;
				Parts parts = (Parts) child;
				for (Node partChild : parts.getChildren()) {
					if (partChild.getKind() != NodeKind.ExpressionPart) {
						break;
					}
					if (start == -1) {
						start = partChild.getStart();						
					}
					end = partChild.getEnd();
				}
				if (start != -1) {
					Template template = node.getOwnerTemplate();
					String name = template.getText(start, end);
					Range range = QutePositionUtility.createRange(start, end, template);
					variables.add(new InlineVariable(name, range));
				}
			}
		}

		return super.visit(node);
	}

	public List<InlineVariable> getVariables() {
		return variables;
	}
}
