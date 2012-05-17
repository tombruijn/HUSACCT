package husacct.analyse.task.analyser.csharp;

import husacct.analyse.infrastructure.antlr.csharp.CSharpParser;
import husacct.analyse.infrastructure.antlr.csharp.CSharpParser.compilation_unit_return;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

class CSharpTreeConvertController extends CSharpGenerator {

	private List<List<CommonTree>> namespaceTrees;
	private List<CommonTree> classTrees;
	private List<List<CommonTree>> usageTrees;
	private List<CommonTree> methodTrees;
	private List<CommonTree> attributeTrees;
	private List<CommonTree> exceptionTrees;
	private List<CommonTree> localVariableTrees;
	private CommonTree abstractTree;
	private boolean isClassName = false;
	private boolean isNamespaceName = false;
	private int indentLevel = 0;
	private int methodIndentLevel = -1;
	private String tempClassName = "";
	private String tempNamespaceName = "";
	private String tempUsingName = "";
	private String tempMethodSignature = "";
	private List<CommonTree> tempNamespaceTrees;
	private List<CommonTree> tempUsingTrees;
	private List<CSharpData> indentClassLevel;
	private List<CSharpData> indentNamespaceLevel;
	private List<CSharpData> indentUsingsLevel; 
	private String tempFullNamespaceName;

	private boolean attributeCheck(CommonTree tree, boolean isPartOfAttribute) {
		int type = tree.getType();
		final int[] notPartOfAttribute = new int[] { FORWARDCURLYBRACKET, USING, NAMESPACE, CLASS, RETURN, SET, GET, DOT };
		final int[] isAPartOfAttribute = new int[] { FORWARDCURLYBRACKET, SEMICOLON, BACKWARDCURLYBRACKET };
		if (isPartOfAttribute && type == SEMICOLON && attributeTrees.size() > 1) {
			startNewAttributeGenerator();
			isPartOfAttribute = false;
			attributeTrees.clear();
		}
		if (Arrays.binarySearch(notPartOfAttribute, type) > -1) {
			isPartOfAttribute = false;
			attributeTrees.clear();
		}
		if (isPartOfAttribute) {
			attributeTrees.add(tree);
		}
		if (Arrays.binarySearch(isAPartOfAttribute, type) > -1) {
			isPartOfAttribute = true;
		}
		return isPartOfAttribute;
	}

	private void startNewAttributeGenerator() {
		String className = tempFullNamespaceName + "." + tempClassName;
		CSharpAttributeGenerator attributeGenerator = new CSharpAttributeGenerator(attributeTrees, className);
		attributeGenerator.scanTree();	
	}

	private void checkData(CSharpData cSharpData, List<CSharpData> indentDataLevel) {
		if (indentLevel == cSharpData.getIntentLevel() && !cSharpData.getClosed()) {
			checkEveryClass(cSharpData, indentDataLevel);
			cSharpData.setClosed(true);
		}
	}

	private void checkEveryClass(CSharpData classData, List<CSharpData> indentDataLevel) {
		for (CSharpData nestedClassData : indentDataLevel) {
			if (classData.getIntentLevel() > nestedClassData.getIntentLevel() && !nestedClassData.getClosed()) {
				String nestedClass = nestedClassData.getClassName();
				CSharpData data = indentDataLevel.get(indentDataLevel.indexOf(classData));
				data.setParentClass(nestedClass);
				data.setHasParent(true);
			}
		}
	}

	private void checkIfClosed(CommonTree tree, List<CSharpData> indentDataLevel) {
		for (CSharpData classData : indentDataLevel) {
			checkData(classData, indentDataLevel);
		}
	}

	private boolean classCheck(CommonTree tree, boolean isClassPart) {
		int type = tree.getType();
		if (type == ABSTRACT) {
			abstractTree = tree;
		}
		if (isClassName) {
			tempClassName = tree.getText();
			for (List<CommonTree> trees : usageTrees) {
				if (!trees.isEmpty() && !indentUsingsLevel.get(usageTrees.indexOf(trees)).getClosed()) {
					new CSharpImportGenerator(trees, tempFullNamespaceName + "." + tempClassName);
				}
			}
			isClassName = false;
		}
		if (type == CLASS || type == INTERFACE || type == STRUCT) {
			isClassPart = true;
			isClassName = true;
		}
		if (isClassPart && type == FORWARDCURLYBRACKET) {
			isClassPart = false;
			indentClassLevel.add(new CSharpData(tempClassName, indentLevel,tempFullNamespaceName));
		}
		if (isClassPart) {
			if (abstractTree != null) {
				classTrees.add(abstractTree);
				abstractTree = null;
			}
			classTrees.add(tree);
		}
		return isClassPart;
	}

	@SuppressWarnings("unchecked")
	public void delegateDomainObjectGenerators(CSharpParser cSharpParser) throws RecognitionException {
		indentClassLevel = new ArrayList<CSharpData>();
		indentNamespaceLevel = new ArrayList<CSharpData>();
		indentUsingsLevel = new ArrayList<CSharpData>();
		compilation_unit_return compilationUnit = cSharpParser.compilation_unit();
		CommonTree compilationUnitTree = (CommonTree) compilationUnit.getTree();
		namespaceTrees = new ArrayList<List<CommonTree>>();
		usageTrees = new ArrayList<List<CommonTree>>();
		classTrees = new ArrayList<CommonTree>();
		attributeTrees = new ArrayList<CommonTree>();
		methodTrees = new ArrayList<CommonTree>();
		exceptionTrees = new ArrayList<CommonTree>();
		localVariableTrees = new ArrayList<CommonTree>();
		walkAST(compilationUnitTree.getChildren());
		new CSharpClassGenerator(classTrees, indentClassLevel);
		CSharpNamespaceGenerator generator = new CSharpNamespaceGenerator();
		for (List<CommonTree> trees : namespaceTrees) {
			generator.namespaceGenerator(trees);
		}
	}

	private void makeFullNamespaceName() {
		tempFullNamespaceName = "";
		for (CommonTree tree : tempNamespaceTrees) {
			if (tree.getType() != NAMESPACE) {
				tempFullNamespaceName += tree.getText();
			}
		}
	}

	private boolean methodCheck(CommonTree tree, boolean isPartOfMethod) {
		int[] ListOfTypes = new int[] { FINAL, PUBLIC, PROTECTED, PRIVATE, ABSTRACT, VOID /* synchronised */};
		int type = tree.getType();
		for (int singleType : ListOfTypes) {
			if (tree.getType() == singleType) {
				isPartOfMethod = true;
			}
		}
		if (type == FORWARDCURLYBRACKET || type == SEMICOLON) {
			isPartOfMethod = false;
			boolean isNewInstance = false;
			boolean hasBrackets = false;
			for (CommonTree methodTree : methodTrees) {
				int methodType = methodTree.getType();
				if (methodType == NEW) {
					isNewInstance = true;
				}
				if (methodType == FORWARDBRACKET) {
					hasBrackets = true;
				}
			}
			if (isNewInstance == false && hasBrackets == true) {
				CSharpMethodGenerator methodGenerator = new CSharpMethodGenerator();
				methodGenerator.generateMethod(methodTrees, tempFullNamespaceName, tempClassName);

				tempMethodSignature = methodGenerator.getSignature();
				methodIndentLevel = indentLevel -1;
			}
			methodTrees.clear();
		}
		if (isPartOfMethod) {
			methodTrees.add(tree);
		}
		return isPartOfMethod;
	}

	private boolean namespaceChecking(CommonTree tree, boolean isNamespacePart) {
		int type = tree.getType();
		if (isNamespaceName) {
			tempNamespaceName = tree.getText();
			isNamespaceName = false;
		}
		if (type == NAMESPACE) {
			isNamespacePart = true;
			isNamespaceName = true;
		}
		if (isNamespacePart && type == FORWARDCURLYBRACKET) {
			makeFullNamespaceName();
			isNamespacePart = false;
			namespaceTrees.add(tempNamespaceTrees);
			tempNamespaceTrees = new ArrayList<CommonTree>();
			indentNamespaceLevel.add(new CSharpData(tempNamespaceName, indentLevel));
		}
		if (isNamespacePart) {
			tempNamespaceTrees.add(tree);
		}
		return isNamespacePart;
	}

	private void setIndentLevel(CommonTree tree) {
		int type = tree.getType();
		if (type == FORWARDCURLYBRACKET) {
			indentLevel++;
		}
		if (type == BACKWARDCURLYBRACKET) {
			checkIfClosed(tree, indentClassLevel);
			checkIfClosed(tree, indentNamespaceLevel);
			checkIfUsingsClosed(tree, indentUsingsLevel);
			indentLevel--;
		}
	}

	private void checkIfUsingsClosed(CommonTree tree,List<CSharpData> indentUsingsLevel) {
		for (CSharpData classData : indentUsingsLevel) {
			if (classData.getIntentLevel() == indentLevel) {
				classData.setClosed(true);
			}
		}
	}

	private boolean usingCheck(CommonTree tree, boolean usage) {
		int type = tree.getType();
		if (type == USING) {
			usage = true;
			usageTrees.add(tempUsingTrees);
			tempUsingTrees = new ArrayList<CommonTree>();
			tempUsingName = "";
		}
		if (usage) {
			if (type != USING) {
				tempUsingTrees.add(tree);
				tempUsingName += tree.getText();
			}
		}
		if (usage && type == SEMICOLON) {
			if (!tempUsingName.equals("")) {
				indentUsingsLevel.add(new CSharpData(tempUsingName,indentLevel));
			}
			usage = false;
		}
		return usage;
	}

	private boolean exceptionCheck(CommonTree tree, boolean isPartOfException){
		int type = tree.getType();

		if(type == CATCH || type == THROW || type == FINALLY){
			isPartOfException = true;
		}

		if(!(exceptionTrees.isEmpty()) && (type == SEMICOLON || type == FORWARDCURLYBRACKET)){
			isPartOfException = false;
			int lineNumber = tree.getLine();
			String uniqueClassName = tempFullNamespaceName + "." + tempClassName;
			CSharpExceptionGenerator exceptionGenerator = new CSharpExceptionGenerator();
			exceptionGenerator.generateException(exceptionTrees, uniqueClassName, lineNumber);
			exceptionTrees.clear();
		}

		if(isPartOfException){
			exceptionTrees.add(tree);

		}
		return isPartOfException;
	}

	private void walkAST(List<CommonTree> children) {
		boolean isPartOfNamespace = false;
		boolean isPartOfClass = false;
		boolean isPartOfUsing = false;
		boolean isPartOfAttribute = false;
		boolean isPartOfMethod = false;
		boolean isPartOfException = false;
		boolean ispartOfLocalVariable = false;
		tempNamespaceTrees = new ArrayList<CommonTree>();
		tempUsingTrees = new ArrayList<CommonTree>();

		for (CommonTree tree : children) {
			setIndentLevel(tree);
			isPartOfNamespace = namespaceChecking(tree, isPartOfNamespace);
			isPartOfClass = classCheck(tree, isPartOfClass);
			isPartOfUsing = usingCheck(tree, isPartOfUsing);
			isPartOfAttribute = attributeCheck(tree, isPartOfAttribute);
			isPartOfMethod = methodCheck(tree, isPartOfMethod);
			isPartOfException = exceptionCheck(tree, isPartOfException);
			if((indentLevel > methodIndentLevel) && methodIndentLevel != -1){
				ispartOfLocalVariable = localVariableCheck(tree, ispartOfLocalVariable);
			}
			
		}
	}

	private boolean localVariableCheck(CommonTree tree, boolean ispartOfLocalVariable) {
		int type = tree.getType();
		
		if(!(ispartOfLocalVariable)){
			for(int thisType : typeCollection){
				if(type == thisType){
					ispartOfLocalVariable = true;
				}
			}
		}
		
		if(type == SEMICOLON || type == IS){
			ispartOfLocalVariable = false;
			int lineNumber = tree.getLine();
			String belongsToClass = tempFullNamespaceName + "." + tempClassName;
			if(localVariableTrees.size() > 0){
				cleanVariableList();
			}
			
			if(localVariableTrees.size() > 1){
				CSharpLocalVariableGenerator localVariableGenerator = new CSharpLocalVariableGenerator();
				localVariableGenerator.generateLocalVariable(localVariableTrees, tempMethodSignature, belongsToClass, lineNumber);
			}
			localVariableTrees.clear();
		}
		
		if(ispartOfLocalVariable){
			localVariableTrees.add(tree);
		}
		
		return ispartOfLocalVariable;
	}

	private void cleanVariableList() {
		boolean isLocalVariable = true;
		final int[] notAllowedTypes = new int[]{DOT, FORWARDCURLYBRACKET, FORWARDBRACKET};
		
		for(CommonTree node : localVariableTrees){
			int type = node.getType();
			
			for (int notAllowedtype : notAllowedTypes){
				if(type == notAllowedtype){
					isLocalVariable = false;
				}
			}
		}
		
		if(!(isLocalVariable)){
			localVariableTrees.clear();
		}
	}
}
