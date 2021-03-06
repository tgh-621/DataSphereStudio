/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package com.webank.wedatasphpere.dss.user.service.impl;

import com.webank.wedatasphpere.dss.user.dto.request.AuthorizationBody;
import com.webank.wedatasphpere.dss.user.service.AbsCommand;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @program: dss-appjoint-auth
 * @description: 开通azkaban账号
 *
 * @create: 2021-01-08 15:53
 **/

public class AzkabanCommand extends AbsCommand {
    @Override
    public String authorization(AuthorizationBody body) {

        try{
            this.xmlHandler(body.getAzkakanDir()+"/conf/azkaban-users.xml", body);
            String[] args = {body.getUsername(), body.getPassword(), body.getDssInstallDir()+"/conf/"};
            String path = getResource("default/AddschedulerUser.sh");
            return this.runShell(path, args);
        }catch (Exception err){
            logger.error("AzkabanCommand auth error:", err);
            return err.getMessage();
        }
    }

    private void xmlHandler(String azkPath, AuthorizationBody body) throws DocumentException, IOException {
        SAXReader reader = new SAXReader();

        File file = new File(azkPath);
        Document document;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            document = reader.read(fis);
        }catch (DocumentException e){
            throw e;
        }finally {
            if (fis != null) {
                fis.close();
            }
        }

        Element root = document.getRootElement();

        Iterator it = root.elementIterator("user");
        Boolean userExists = false;
        Element element = null;
        while (it.hasNext()) {
            element = (Element) it.next();

            String v = element.attributeValue("username");
            if(v.equals(body.getUsername())){       //修改密码
                userExists = true;
                element.attribute("password").setValue(body.getPassword());
            }
        }
        if(!userExists){    //新增账号
            Element cloneEl = element.createCopy();
            cloneEl.attribute("username").setValue(body.getUsername());
            cloneEl.attribute("password").setValue(body.getPassword());

            List elements  = root.elements("user");
            elements.add(elements.size(), cloneEl);
        }

        this.saveXml(document, file);

    }

    private void saveXml(Document document, File file) throws IOException {
        FileOutputStream out =new FileOutputStream(file);
        OutputFormat format=OutputFormat.createPrettyPrint();
        XMLWriter writer=new XMLWriter(out, format);
        writer.write(document);
        writer.close();
    }
}
