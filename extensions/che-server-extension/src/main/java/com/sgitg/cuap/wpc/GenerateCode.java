/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.sgitg.cuap.wpc;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import com.sgitg.cuap.wpc.codejet.CodeJet;
import com.sgitg.cuap.wpc.codejet.core.framework.ExpertTeam;
import com.sgitg.cuap.wpc.codejet.scanner.IRootLocator;
import com.sgitg.cuap.wpc.common.PrintException;
import com.sgitg.cuap.wpc.common.generator.UapExpertTeam;
import com.sgitg.cuap.wpc.designer.common.biz.impl.SpaceRootLocator;


@Path("/generateCodeService")
public class GenerateCode {
	
	@Inject
	public GenerateCode(){
	}	
	//=====================================//
	@Path("/genMxOriginalCode/{a},{b},{c},{d},{e}")
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Map<String, String> genMxOriginalCode(@PathParam("a") String editSpaceId,@PathParam("b") String editProjectName,@PathParam("c") String packageName,@PathParam("d") String hostInfo,@PathParam("e") String logicFrameType) {
		System.out.println("--------------------进入类GenerateCode;方法genMxOriginalCode--------------------");
		Map<String, String> rs = new HashMap<String, String>();
		try {
			System.out.println("spaceId=="+editSpaceId);
			System.out.println("projectName=="+editProjectName);
			System.out.println("packageName=="+packageName);
			System.out.println("hostInfo=="+hostInfo);
			System.out.println("logicFrameType=="+logicFrameType);
			//获取服务的url ip及端口号； "http://192.168.8.119:9000/cuap/dmm-datamodel/rest/define/getModelXMLInfo";
//			String getModelXMLUrl="http://"+host+":"+port+"/cuap/dmm-datamodel/rest/define/getModelXMLInfo";
			IRootLocator rootLocator = new SpaceRootLocator(editSpaceId,"http://"+hostInfo);
			System.out.println("111111111111111111");
			//实例化某技术框架的ExpertTeam
			ExpertTeam team= new UapExpertTeam(packageName, rootLocator);//包路径
			System.out.println("222222222222222222");
			CodeJet enginee = new CodeJet(team, rootLocator);
			System.out.println("333333333333333333333");
			String projTeam="";//考虑到项目组下的所有数据模型，但若数据模型全局唯一就不需此参数。
			enginee.genCode(projTeam, editSpaceId, editProjectName);
			System.out.println("44444444444444444444");
			//excute translate
			rs.put("success", "翻译成功！");
		} catch (Exception e) {
			System.out.println("55555555555555");
			String msg = e.getMessage();
			PrintException.getErrorMessage(e);
			rs.put("failed", msg.substring(msg.indexOf(":")+1)+"！");
		}
		System.out.println("7777777777777777777777777777");
		return rs;
	}
}
