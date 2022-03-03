package com.redhat.qute.jdt.internal.extensions.renarde;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.jdt.template.datamodel.AbstractDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

public class UriNamespaceResolverSupport extends AbstractDataModelProvider {

	@Override
	public void beginSearch(SearchContext context, IProgressMonitor monitor) {
		IJavaProject javaProject = context.getJavaProject();
		IType type = JDTTypeUtils.findType(javaProject, "io.quarkiverse.renarde.Controller");
		if (type != null) {
			try {
				ITypeHierarchy typeHierarchy = type.newTypeHierarchy(monitor);
				IType[] controllers = typeHierarchy.getAllClasses();
				List<ValueResolverInfo> resolvers = context.getDataModelProject().getValueResolvers();
				for (IType controller : controllers) {
					ValueResolverInfo resolver = new ValueResolverInfo();
					resolver.setNamed(controller.getElementName());
					resolver.setSourceType(controller.getFullyQualifiedName());
					resolver.setSignature(controller.getFullyQualifiedName());					
					resolver.setNamespace("uri");
					resolvers.add(resolver);
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@Override
	public void collectDataModel(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String[] getPatterns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SearchPattern createSearchPattern(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

}
