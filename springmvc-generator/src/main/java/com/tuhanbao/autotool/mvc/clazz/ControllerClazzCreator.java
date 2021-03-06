package com.tuhanbao.autotool.mvc.clazz;

import com.tuhanbao.Constants;
import com.tuhanbao.autotool.mvc.J2EEProjectInfo;
import com.tuhanbao.autotool.mvc.J2EETable;
import com.tuhanbao.autotool.mvc.ProjectConfig;
import com.tuhanbao.io.impl.classUtil.ClassInfo;
import com.tuhanbao.io.impl.classUtil.MethodInfo;
import com.tuhanbao.io.impl.classUtil.PackageEnum;
import com.tuhanbao.io.impl.classUtil.VarInfo;
import com.tuhanbao.io.impl.tableUtil.ImportColumn;
import com.tuhanbao.util.util.clazz.ClazzUtil;


public class ControllerClazzCreator extends ClazzCreator{

	public ControllerClazzCreator(J2EEProjectInfo project) {
		super(project);
	}

	@Override
	public ClassInfo toClazz(J2EETable table) {
		ClassInfo clazz = new ClassInfo();
		clazz.setName(table.getControllerName());
		clazz.setPackageInfo(project.getControllerPath(table.getModule()));
		clazz.addImportInfo("org.springframework.stereotype.Controller");
		clazz.addImportInfo("org.springframework.web.bind.annotation.RequestMapping");
		clazz.addImportInfo("org.springframework.web.bind.annotation.ResponseBody");
		clazz.addImportInfo("org.springframework.beans.factory.annotation.Autowired");
		clazz.addImportInfo(project.getIServicePath(table.getModule()) + Constants.STOP_EN + table.getIServiceName());
		clazz.addImportInfo(project.getServiceBeanPath(table.getModule()) + Constants.STOP_EN + table.getModelName());
		clazz.addAnnotation("@Controller");
		String requestMap = "@RequestMapping(\""+ ClazzUtil.firstCharLowerCase(table.getModelName()) + "\")";
		clazz.addAnnotation(requestMap);
		
		VarInfo varInfo = new VarInfo();
		varInfo.addAnnotation("@Autowired");
		varInfo.setType(table.getIServiceName());
		String serviceVarName = ClazzUtil.firstCharLowerCase(table.getIServiceName().substring(1));
		varInfo.setName(serviceVarName);
		varInfo.setPe(PackageEnum.PRIVATE);
		clazz.addVarInfo(varInfo);
		
		MethodInfo add = createAddMethod(serviceVarName, table.getModelName());
		ImportColumn pk = table.getPK();
		if (pk != null) {
			MethodInfo delete = createDeleteMethod(serviceVarName, table.getModelName(), pk);
			clazz.addMethodInfo(delete);
		}
		MethodInfo search = createListMethod(serviceVarName, table.getModelName());
		MethodInfo update = createUpdateMethod(serviceVarName, table.getModelName());
		clazz.addMethodInfo(add);
		clazz.addMethodInfo(search);
		clazz.addMethodInfo(update);

		if (ProjectConfig.NEED_CREATE_WEB) {
			MethodInfo index = createIndexMethod(ClazzUtil.firstCharLowerCase(table.getModelName()));
			clazz.addMethodInfo(index);
		}
		return clazz;
	}
	
	private MethodInfo createIndexMethod(String modelName){
		MethodInfo method = new MethodInfo();
		String annotation = "@RequestMapping(\"index\")";
		method.addAnnotation(annotation);
		method.setPe(PackageEnum.PUBLIC);
		method.setType("Object");
		method.setName("index");
		method.setMethodBody("return \"" + modelName + "\";");
		return method;
	}
	
	private MethodInfo createAddMethod(String serviceName , String modelName){
		MethodInfo method = new MethodInfo();
		String annotation = "@RequestMapping(\"add\")";
		method.addAnnotation(annotation);
		method.setPe(PackageEnum.PUBLIC);
		method.addAnnotation("@ResponseBody");
		method.setType("Object");
		method.setName("add");
		method.setArgs(modelName +" " + ClazzUtil.getVarName(modelName));
		StringBuilder methodBody = new StringBuilder();
		methodBody.append( serviceName +".add(" + ClazzUtil.getVarName(modelName) + ");").append(Constants.ENTER);
		methodBody.append(gap2).append("return null;").append(Constants.ENTER);
		method.setMethodBody(methodBody.toString());
		return method;
	}
	
	private MethodInfo createDeleteMethod(String serviceName , String modelName, ImportColumn pk){
		MethodInfo method = new MethodInfo();
		String annotation = "@RequestMapping(\"delete\")";
		method.addAnnotation(annotation);
		method.setPe(PackageEnum.PUBLIC);
		method.addAnnotation("@ResponseBody");
		method.setType("Object");
		method.setName("delete");
		method.setArgs(pk.getDataType().getName() + " id");
		StringBuilder methodBody = new StringBuilder();
		methodBody.append("return " + serviceName +".deleteById(id);").append(Constants.ENTER);
		method.setMethodBody(methodBody.toString());
		return method;
	}

	private MethodInfo createUpdateMethod(String serviceName , String modelName){
		MethodInfo method = new MethodInfo();
		String annotation = "@RequestMapping(\"edit\")";
		method.addAnnotation(annotation);
		method.setPe(PackageEnum.PUBLIC);
		method.addAnnotation("@ResponseBody");
		method.setType("Object");
		method.setName("edit");
		method.setArgs(modelName +" " + ClazzUtil.getVarName(modelName));
		StringBuilder methodBody = new StringBuilder();
		methodBody.append("return " + serviceName +".update(" + ClazzUtil.getVarName(modelName) + ");").append(Constants.ENTER);
		method.setMethodBody(methodBody.toString());
		return method;
	}
	
	private MethodInfo createListMethod(String serviceName , String modelName){
		MethodInfo method = new MethodInfo();
		String annotation = "@RequestMapping(\"list\")";
		method.addAnnotation(annotation);
		method.setPe(PackageEnum.PUBLIC);
		method.addAnnotation("@ResponseBody");
		method.setType("Object");
		method.setName("list");
		StringBuilder methodBody = new StringBuilder();
		methodBody.append("return ").append(serviceName +".select(null);").append(Constants.ENTER);
		method.setMethodBody(methodBody.toString());
		return method;
	}

}
