package com.keven1z.core.policy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.keven1z.core.Config;
import com.keven1z.core.utils.FileUtils;
import com.keven1z.core.utils.JsonUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 负责资源文件加载
 */
public class FileLoader {

    /**
     * 从policy.json加载hook策略
     *
     * @return {@link PolicyContainer}
     */
    public static PolicyContainer load(ClassLoader classLoader) throws JsonProcessingException {
        InputStream inputStream = null;
        try {
            inputStream = classLoader.getResourceAsStream(Config.POLICY_FILE_PATH);
            String jsonFile = FileUtils.readJsonFile(inputStream);
            if (jsonFile == null) {
                return null;
            }

            PolicyContainer policyContainer = JsonUtils.toObject(jsonFile, PolicyContainer.class);

            if (policyContainer == null) {
                return null;
            }

            List<Policy> sources = policyContainer.getSource();
            List<Policy> interfacePolicy = policyContainer.getInterfacePolicy();
            for (Policy source : sources) {
                if (source.getInter()) {
                    interfacePolicy.add(source);
                }
                source.setType(PolicyTypeEnum.SOURCE);
            }
            List<Policy> propagations = policyContainer.getPropagation();
            for (Policy propagation : propagations) {
                if (propagation.getInter()) {
                    interfacePolicy.add(propagation);
                }
                propagation.setType(PolicyTypeEnum.PROPAGATION);
            }
            List<Policy> sinks = policyContainer.getSink();
            for (Policy sink : sinks) {
                if (sink.getInter()) {
                    interfacePolicy.add(sink);
                }
                sink.setType(PolicyTypeEnum.SINK);
            }
            List<Policy> https = policyContainer.getHttp();
            for (Policy http : https) {
                if (http.getInter()) {
                    interfacePolicy.add(http);
                }
                http.setType(PolicyTypeEnum.HTTP);
            }
            List<Policy> sanitizers = policyContainer.getSanitizers();
            for (Policy sanitizer : sanitizers) {
                if (sanitizer.getInter()) {
                    interfacePolicy.add(sanitizer);
                }
                sanitizer.setType(PolicyTypeEnum.SANITIZER);
            }

            return policyContainer;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
     * @return 加载黑名单文件
     */
    public static List<String> loadBlackList(ClassLoader classLoader) {
        ArrayList<String> arrayList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classLoader.getResourceAsStream(Config.BLACK_LIST_FILE_PATH))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("\n", "");
                if (!"".equals(line)) {
                    arrayList.add(line);
                }
            }
        } catch (IOException e) {
            return arrayList;
        }
        return arrayList;
    }


}
