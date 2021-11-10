package com.redhat.qute.utils;

import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.BaseWhenSection;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.parser.template.sections.WithSection;

public class QuteSearchUtils {

	public static void searchDeclaredObjects(ObjectPart part, BiConsumer<Node, Range> collector, boolean includeNode,
			CancelChecker cancelChecker) {
		if (includeNode) {
			Range range = QutePositionUtility.createRange(part);
			collector.accept(part, range);
		}
		JavaTypeInfoProvider resolvedJavaType = ((ObjectPart) part).resolveJavaType();
		if (resolvedJavaType != null) {
			Node node = resolvedJavaType.getJavaTypeOwnerNode();
			if (node != null) {
				switch (node.getKind()) {
				case ParameterDeclaration: {
					ParameterDeclaration parameter = (ParameterDeclaration) node;
					Range targetRange = QutePositionUtility.selectAlias(parameter);
					collector.accept(parameter, targetRange);
					break;
				}
				case Parameter: {
					Parameter parameter = (Parameter) node;
					Range targetRange = QutePositionUtility.selectParameterName(parameter);
					collector.accept(parameter, targetRange);
					break;
				}
				default:
				}
			}
		}
	}

	public static void searchReferencedObjects(Node node, int offset, BiConsumer<Node, Range> collector,
			CancelChecker cancelChecker) {
		searchReferencedObjects(node, offset, collector, false, cancelChecker);
	}

	public static void searchReferencedObjects(Node node, int offset, BiConsumer<Node, Range> collector,
			boolean includeNode, CancelChecker cancelChecker) {
		switch (node.getKind()) {
		case ParameterDeclaration:
			ParameterDeclaration parameterDeclaration = (ParameterDeclaration) node;
			if (parameterDeclaration.isInAlias(offset)) {
				String alias = parameterDeclaration.getAlias();
				if (includeNode) {
					Range range = QutePositionUtility.selectAlias(parameterDeclaration);
					collector.accept(parameterDeclaration, range);
				}
				searchReferencedObjects(alias, node, collector, cancelChecker);
			}
			break;
		case Parameter:
			Parameter parameter = (Parameter) node;
			searchReferencedObjects(parameter, includeNode, collector, cancelChecker);
			break;
		}
	}

	private static void searchReferencedObjects(Parameter parameter, boolean includeNode,
			BiConsumer<Node, Range> collector, CancelChecker cancelChecker) {
		if (includeNode) {
			Range range = QutePositionUtility.selectParameterName(parameter);
			collector.accept(parameter, range);
		}
		String alias = parameter.getName();
		searchReferencedObjects(alias, parameter.getParent(), collector, cancelChecker);
	}

	private static void searchReferencedObjects(String partName, Node owerNode, BiConsumer<Node, Range> collector,
			CancelChecker cancelChecker) {
		Template template = owerNode.getOwnerTemplate();
		Node parent = owerNode.getKind() == NodeKind.ParameterDeclaration ? template : owerNode;
		searchReferencedObjects(partName, parent, owerNode, collector, cancelChecker);
	}

	private static void searchReferencedObjects(String partName, Node parent, Node owerNode,
			BiConsumer<Node, Range> collector, CancelChecker cancelChecker) {
		if (parent != owerNode) {
			switch (parent.getKind()) {
			case Expression:
				Expression expression = (Expression) parent;
				tryToCollectObjectPart(partName, expression, collector);
				break;
			case Section:
				Section section = (Section) parent;
				switch (section.getSectionKind()) {
				case EACH:
				case FOR: {
					LoopSection loopSection = (LoopSection) parent;
					Parameter parameter = loopSection.getIterableParameter();
					Expression parameterExpr = parameter.getJavaTypeExpression();
					tryToCollectObjectPart(partName, parameterExpr, collector);
					break;
				}
				case LET:
				case SET: {
					List<Parameter> parameters = section.getParameters();
					if (parameters != null) {
						for (Parameter parameter : parameters) {
							if (parameter != null && parameter.hasValueAssigned()) {
								Expression parameterExpr = parameter.getJavaTypeExpression();
								tryToCollectObjectPart(partName, parameterExpr, collector);
							}
						}
					}
					break;
				}
				case WITH: {
					Parameter parameter = ((WithSection) section).getObjectParameter();
					Expression parameterExpr = parameter.getJavaTypeExpression();
					tryToCollectObjectPart(partName, parameterExpr, collector);
					break;
				}
				case WHEN:
				case SWITCH: {
					Parameter parameter = ((BaseWhenSection) section).getValueParameter();
					Expression parameterExpr = parameter.getJavaTypeExpression();
					tryToCollectObjectPart(partName, parameterExpr, collector);
					break;
				}
				default:
				}
			default:
			}
		}

		List<Node> children = parent.getChildren();
		for (Node node : children) {
			searchReferencedObjects(partName, node, owerNode, collector, cancelChecker);
		}
	}

	private static void tryToCollectObjectPart(String partName, Expression parameterExpr,
			BiConsumer<Node, Range> collector) {
		ObjectPart objectPart = parameterExpr.getObjectPart();
		if (objectPart != null && partName.equals(objectPart.getPartName())) {
			Range range = QutePositionUtility.createRange(objectPart);
			collector.accept(objectPart, range);
		}
	}
}
