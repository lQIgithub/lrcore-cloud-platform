//package com.lrcore.system.adapter.workflow;
//
//import com.lrcore.system.domain.workflow.definition.FlowEdge;
//import com.lrcore.system.domain.workflow.definition.FlowGraphData;
//import com.lrcore.system.domain.workflow.definition.FlowNode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
/// **
// * LogicFlow到Flowable BPMN适配器测试类
// */
//@SpringBootTest
//class LogicFlowToFlowableBpmnAdapterTest {
//
//    @Autowired
//    private LogicFlowToFlowableAdapter adapter;
//
//    private FlowGraphData testFlowGraphData;
//
//    @BeforeEach
//    void setUp() {
//        testFlowGraphData = createBasicFlowGraphData();
//    }
//
//    @Nested
//    @DisplayName("基本转换功能测试")
//    class BasicConversionTest {
//
//        @Test
//        @DisplayName("测试基本转换功能")
//        void testAdapt() {
//            String bpmnXml = adapter.adapt(testFlowGraphData);
//
//            assertNotNull(bpmnXml);
//            assertTrue(bpmnXml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
//            assertTrue(bpmnXml.contains("<definitions"));
//            assertTrue(bpmnXml.contains("<process"));
//            assertTrue(bpmnXml.contains("<startEvent id=\"start1\""));
//            assertTrue(bpmnXml.contains("<userTask id=\"task1\""));
//            assertTrue(bpmnXml.contains("<endEvent id=\"end1\""));
//            assertTrue(bpmnXml.contains("<sequenceFlow id=\"edge1\""));
//            assertTrue(bpmnXml.contains("<sequenceFlow id=\"edge2\""));
//        }
//
//        @Test
//        @DisplayName("测试转换包含负责人属性")
//        void testAdaptWithAssignee() {
//            String bpmnXml = adapter.adapt(testFlowGraphData);
//            assertTrue(bpmnXml.contains("flowable:assignee=\"${assignee}\""));
//        }
//
//        @Test
//        @DisplayName("测试无效数据转换抛出异常")
//        void testAdaptWithInvalidData() {
//            FlowGraphData invalidData = new FlowGraphData();
//            assertThrows(IllegalArgumentException.class, () -> adapter.adapt(invalidData));
//        }
//    }
//
//    @Nested
//    @DisplayName("验证功能测试")
//    class ValidationTest {
//
//        @Test
//        @DisplayName("测试有效数据验证")
//        void testValidateValidData() {
//            assertTrue(adapter.validate(testFlowGraphData));
//        }
//
//        @Test
//        @DisplayName("测试null数据验证")
//        void testValidateNullData() {
//            assertFalse(adapter.validate(null));
//        }
//
//        @Test
//        @DisplayName("测试空节点列表验证")
//        void testValidateEmptyNodes() {
//            FlowGraphData emptyData = new FlowGraphData();
//            emptyData.setNodes(Collections.emptyList());
//            assertFalse(adapter.validate(emptyData));
//        }
//
//        @Test
//        @DisplayName("测试没有开始节点验证")
//        void testValidateNoStartNode() {
//            FlowGraphData noStartData = new FlowGraphData();
//            FlowNode taskNode = createNode("task1", "task", "任务");
//            FlowNode endNode = createNode("end1", "end", "结束");
//            noStartData.setNodes(Arrays.asList(taskNode, endNode));
//            assertFalse(adapter.validate(noStartData));
//        }
//
//        @Test
//        @DisplayName("测试没有结束节点验证")
//        void testValidateNoEndNode() {
//            FlowGraphData noEndData = new FlowGraphData();
//            FlowNode startNode = createNode("start1", "start", "开始");
//            FlowNode taskNode = createNode("task1", "task", "任务");
//            noEndData.setNodes(Arrays.asList(startNode, taskNode));
//            assertFalse(adapter.validate(noEndData));
//        }
//
//        @Test
//        @DisplayName("测试重复节点ID验证")
//        void testValidateDuplicateNodeId() {
//            FlowGraphData dupData = new FlowGraphData();
//            FlowNode startNode = createNode("start1", "start", "开始");
//            FlowNode dupNode = createNode("start1", "end", "结束");
//            dupData.setNodes(Arrays.asList(startNode, dupNode));
//            assertFalse(adapter.validate(dupData));
//        }
//
//        @Test
//        @DisplayName("测试空节点ID验证")
//        void testValidateEmptyNodeId() {
//            FlowGraphData emptyIdData = new FlowGraphData();
//            FlowNode startNode = new FlowNode();
//            startNode.setId("");
//            startNode.setType("start");
//            startNode.setText("开始");
//            FlowNode endNode = createNode("end1", "end", "结束");
//            emptyIdData.setNodes(Arrays.asList(startNode, endNode));
//            assertFalse(adapter.validate(emptyIdData));
//        }
//
//        @Test
//        @DisplayName("测试不支持的节点类型验证")
//        void testValidateUnsupportedNodeType() {
//            FlowGraphData unsupportedData = new FlowGraphData();
//            FlowNode startNode = createNode("start1", "start", "开始");
//            FlowNode unsupportedNode = createNode("unsupported1", "unsupportedType", "不支持的类型");
//            FlowNode endNode = createNode("end1", "end", "结束");
//            unsupportedData.setNodes(Arrays.asList(startNode, unsupportedNode, endNode));
//            assertFalse(adapter.validate(unsupportedData));
//        }
//
//        @Test
//        @DisplayName("测试连线引用不存在的节点验证")
//        void testValidateEdgeReferencingNonExistentNode() {
//            FlowGraphData edgeData = new FlowGraphData();
//            FlowNode startNode = createNode("start1", "start", "开始");
//            FlowNode endNode = createNode("end1", "end", "结束");
//            FlowEdge edge = new FlowEdge();
//            edge.setId("edge1");
//            edge.setSourceNodeId("start1");
//            edge.setTargetNodeId("nonExistentNode");
//            edgeData.setNodes(Arrays.asList(startNode, endNode));
//            edgeData.setEdges(Arrays.asList(edge));
//            assertFalse(adapter.validate(edgeData));
//        }
//
//        @Test
//        @DisplayName("测试重复连线ID验证")
//        void testValidateDuplicateEdgeId() {
//            FlowGraphData dupEdgeData = createBasicFlowGraphData();
//            FlowEdge dupEdge = new FlowEdge();
//            dupEdge.setId("edge1");
//            dupEdge.setSourceNodeId("start1");
//            dupEdge.setTargetNodeId("task1");
//            List<FlowEdge> edges = new ArrayList<>(dupEdgeData.getEdges());
//            edges.add(dupEdge);
//            dupEdgeData.setEdges(edges);
//            assertFalse(adapter.validate(dupEdgeData));
//        }
//    }
//
//    @Nested
//    @DisplayName("节点类型转换测试")
//    class NodeTypeConversionTest {
//
//        @Test
//        @DisplayName("测试开始事件节点转换")
//        void testStartEventConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("start", "开始节点");
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<startEvent"));
//        }
//
//        @Test
//        @DisplayName("测试结束事件节点转换")
//        void testEndEventConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("end", "结束节点");
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<endEvent"));
//        }
//
//        @Test
//        @DisplayName("测试用户任务节点转换")
//        void testUserTaskConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("userTask", "用户任务",
//                Map.of("assignee", "${user}"));
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<userTask"));
//            assertTrue(bpmnXml.contains("flowable:assignee=\"${user}\""));
//        }
//
//        @Test
//        @DisplayName("测试服务任务节点转换")
//        void testServiceTaskConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("serviceTask", "服务任务",
//                Map.of("class", "com.example.MyDelegate"));
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<serviceTask"));
//            assertTrue(bpmnXml.contains("flowable:class=\"com.example.MyDelegate\""));
//        }
//
//        @Test
//        @DisplayName("测试脚本任务节点转换")
//        void testScriptTaskConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("scriptTask", "脚本任务",
//                Map.of("scriptFormat", "javascript", "script", "print('hello')"));
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<scriptTask"));
//            assertTrue(bpmnXml.contains("scriptFormat=\"javascript\""));
//            assertTrue(bpmnXml.contains("<script>print('hello')</script>"));
//        }
//
//        @Test
//        @DisplayName("测试业务规则任务节点转换")
//        void testBusinessRuleTaskConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("businessRuleTask", "业务规则任务",
//                Map.of("decisionRef", "rule1"));
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<businessRuleTask"));
//            assertTrue(bpmnXml.contains("flowable:decisionRef=\"rule1\""));
//        }
//
//        @Test
//        @DisplayName("测试手动任务节点转换")
//        void testManualTaskConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("manualTask", "手动任务");
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<manualTask"));
//        }
//
//        @Test
//        @DisplayName("测试接收任务节点转换")
//        void testReceiveTaskConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("receiveTask", "接收任务",
//                Map.of("messageRef", "message1"));
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<receiveTask"));
//            assertTrue(bpmnXml.contains("messageRef=\"message1\""));
//        }
//
//        @Test
//        @DisplayName("测试发送任务节点转换")
//        void testSendTaskConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("sendTask", "发送任务",
//                Map.of("messageRef", "message1"));
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<sendTask"));
//            assertTrue(bpmnXml.contains("messageRef=\"message1\""));
//        }
//
//        @Test
//        @DisplayName("测试调用活动节点转换")
//        void testCallActivityConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("callActivity", "调用活动",
//                Map.of("calledElement", "subProcess1"));
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<callActivity"));
//            assertTrue(bpmnXml.contains("calledElement=\"subProcess1\""));
//        }
//
//        @Test
//        @DisplayName("测试子流程节点转换")
//        void testSubProcessConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("subProcess", "子流程");
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<subProcess"));
//        }
//
//        @Test
//        @DisplayName("测试排他网关节点转换")
//        void testExclusiveGatewayConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("exclusiveGateway", "排他网关");
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<exclusiveGateway"));
//        }
//
//        @Test
//        @DisplayName("测试并行网关节点转换")
//        void testParallelGatewayConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("parallelGateway", "并行网关");
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<parallelGateway"));
//        }
//
//        @Test
//        @DisplayName("测试包含网关节点转换")
//        void testInclusiveGatewayConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("inclusiveGateway", "包含网关");
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<inclusiveGateway"));
//        }
//
//        @Test
//        @DisplayName("测试基于事件网关节点转换")
//        void testEventBasedGatewayConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("eventBasedGateway", "事件网关");
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<eventBasedGateway"));
//        }
//
//        @Test
//        @DisplayName("测试复杂网关节点转换")
//        void testComplexGatewayConversion() {
//            FlowGraphData data = createFlowGraphDataWithNode("complexGateway", "复杂网关",
//                Map.of("default", "flow1"));
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<complexGateway"));
//            assertTrue(bpmnXml.contains("default=\"flow1\""));
//        }
//    }
//
//    @Nested
//    @DisplayName("属性映射测试")
//    class PropertyMappingTest {
//
//        @Test
//        @DisplayName("测试用户任务属性映射")
//        void testUserTaskProperties() {
//            Map<String, Object> properties = new HashMap<>();
//            properties.put("assignee", "${assignee}");
//            properties.put("candidateUsers", "user1,user2");
//            properties.put("candidateGroups", "group1,group2");
//            properties.put("dueDate", "2024-12-31");
//            properties.put("priority", "50");
//
//            FlowGraphData data = createFlowGraphDataWithNode("userTask", "审批", properties);
//            String bpmnXml = adapter.adapt(data);
//
//            assertTrue(bpmnXml.contains("flowable:assignee=\"${assignee}\""));
//            assertTrue(bpmnXml.contains("flowable:candidateUsers=\"user1,user2\""));
//            assertTrue(bpmnXml.contains("flowable:candidateGroups=\"group1,group2\""));
//            assertTrue(bpmnXml.contains("flowable:dueDate=\"2024-12-31\""));
//            assertTrue(bpmnXml.contains("flowable:priority=\"50\""));
//        }
//
//        @Test
//        @DisplayName("测试服务任务属性映射")
//        void testServiceTaskProperties() {
//            Map<String, Object> properties = new HashMap<>();
//            properties.put("delegateExpression", "${myDelegate}");
//            properties.put("class", "com.example.MyDelegate");
//            properties.put("expression", "${myBean.execute()}");
//
//            FlowGraphData data = createFlowGraphDataWithNode("serviceTask", "服务", properties);
//            String bpmnXml = adapter.adapt(data);
//
//            assertTrue(bpmnXml.contains("flowable:delegateExpression=\"${myDelegate}\""));
//            assertTrue(bpmnXml.contains("flowable:class=\"com.example.MyDelegate\""));
//            assertTrue(bpmnXml.contains("flowable:expression=\"${myBean.execute()}\""));
//        }
//
//        @Test
//        @DisplayName("测试连线条件表达式")
//        void testEdgeConditionExpression() {
//            FlowGraphData data = createBasicFlowGraphData();
//            FlowEdge edge = data.getEdges().get(0);
//            Map<String, Object> edgeProperties = new HashMap<>();
//            edgeProperties.put("conditionExpression", "${approved == true}");
//            edge.setProperties(edgeProperties);
//
//            String bpmnXml = adapter.adapt(data);
//            assertTrue(bpmnXml.contains("<conditionExpression"));
//            assertTrue(bpmnXml.contains("${approved == true}"));
//        }
//    }
//
//    @Nested
//    @DisplayName("XML特殊字符处理测试")
//    class XmlSpecialCharTest {
//
//        @Test
//        @DisplayName("测试XML特殊字符转义")
//        void testXmlSpecialCharEscaping() {
//            FlowGraphData data = new FlowGraphData();
//            FlowNode startNode = createNode("start1", "start", "开始");
//            FlowNode taskNode = createNode("task1", "task", "任务 <测试> \"特殊\" & 字符");
//            FlowNode endNode = createNode("end1", "end", "结束");
//            data.setNodes(Arrays.asList(startNode, taskNode, endNode));
//            data.setEdges(Collections.emptyList());
//
//            String bpmnXml = adapter.adapt(data);
//            assertNotNull(bpmnXml);
//            assertTrue(bpmnXml.contains("&amp;"));
//            assertTrue(bpmnXml.contains("&lt;"));
//            assertTrue(bpmnXml.contains("&gt;"));
//            assertTrue(bpmnXml.contains("&quot;"));
//        }
//    }
//
//    @Nested
//    @DisplayName("接口信息测试")
//    class InterfaceInfoTest {
//
//        @Test
//        @DisplayName("测试获取支持的节点类型")
//        void testGetSupportedNodeTypes() {
//            String[] nodeTypes = adapter.getSupportedNodeTypes();
//            assertNotNull(nodeTypes);
//            assertTrue(nodeTypes.length >= 17);
//            assertTrue(Arrays.asList(nodeTypes).contains("start"));
//            assertTrue(Arrays.asList(nodeTypes).contains("end"));
//            assertTrue(Arrays.asList(nodeTypes).contains("task"));
//            assertTrue(Arrays.asList(nodeTypes).contains("userTask"));
//            assertTrue(Arrays.asList(nodeTypes).contains("serviceTask"));
//            assertTrue(Arrays.asList(nodeTypes).contains("scriptTask"));
//            assertTrue(Arrays.asList(nodeTypes).contains("businessRuleTask"));
//            assertTrue(Arrays.asList(nodeTypes).contains("manualTask"));
//            assertTrue(Arrays.asList(nodeTypes).contains("receiveTask"));
//            assertTrue(Arrays.asList(nodeTypes).contains("sendTask"));
//            assertTrue(Arrays.asList(nodeTypes).contains("callActivity"));
//            assertTrue(Arrays.asList(nodeTypes).contains("subProcess"));
//            assertTrue(Arrays.asList(nodeTypes).contains("exclusiveGateway"));
//            assertTrue(Arrays.asList(nodeTypes).contains("parallelGateway"));
//            assertTrue(Arrays.asList(nodeTypes).contains("inclusiveGateway"));
//            assertTrue(Arrays.asList(nodeTypes).contains("eventBasedGateway"));
//            assertTrue(Arrays.asList(nodeTypes).contains("complexGateway"));
//        }
//
//        @Test
//        @DisplayName("测试获取支持的连线类型")
//        void testGetSupportedEdgeTypes() {
//            String[] edgeTypes = adapter.getSupportedEdgeTypes();
//            assertNotNull(edgeTypes);
//            assertTrue(edgeTypes.length >= 3);
//            assertTrue(Arrays.asList(edgeTypes).contains("sequenceFlow"));
//            assertTrue(Arrays.asList(edgeTypes).contains("conditionalFlow"));
//            assertTrue(Arrays.asList(edgeTypes).contains("defaultFlow"));
//        }
//
//        @Test
//        @DisplayName("测试适配器名称")
//        void testGetName() {
//            assertNotNull(adapter.getName());
//            assertEquals("LogicFlow to Flowable BPMN 2.0 Adapter", adapter.getName());
//        }
//
//        @Test
//        @DisplayName("测试适配器版本")
//        void testGetVersion() {
//            assertNotNull(adapter.getVersion());
//            assertEquals("1.0.0", adapter.getVersion());
//        }
//    }
//
//    @Nested
//    @DisplayName("边界条件测试")
//    class BoundaryTest {
//
//        @Test
//        @DisplayName("测试空连线列表转换")
//        void testEmptyEdgesConversion() {
//            FlowGraphData data = new FlowGraphData();
//            FlowNode startNode = createNode("start1", "start", "开始");
//            FlowNode taskNode = createNode("task1", "task", "任务");
//            FlowNode endNode = createNode("end1", "end", "结束");
//            data.setNodes(Arrays.asList(startNode, taskNode, endNode));
//            data.setEdges(Collections.emptyList());
//
//            String bpmnXml = adapter.adapt(data);
//            assertNotNull(bpmnXml);
//            assertTrue(bpmnXml.contains("<startEvent"));
//            assertTrue(bpmnXml.contains("<userTask"));
//            assertTrue(bpmnXml.contains("<endEvent"));
//        }
//
//        @Test
//        @DisplayName("测试节点中文本内容转换")
//        void testChineseTextConversion() {
//            FlowGraphData data = new FlowGraphData();
//            FlowNode startNode = createNode("start1", "start", "开始节点");
//            FlowNode taskNode = createNode("task1", "task", "审批任务");
//            FlowNode endNode = createNode("end1", "end", "结束节点");
//            data.setNodes(Arrays.asList(startNode, taskNode, endNode));
//            data.setEdges(Collections.emptyList());
//
//            String bpmnXml = adapter.adapt(data);
//            assertNotNull(bpmnXml);
//            assertTrue(bpmnXml.contains("开始节点"));
//            assertTrue(bpmnXml.contains("审批任务"));
//            assertTrue(bpmnXml.contains("结束节点"));
//        }
//
//        @Test
//        @DisplayName("测试无属性节点转换")
//        void testNodeWithoutProperties() {
//            FlowGraphData data = new FlowGraphData();
//            FlowNode startNode = createNode("start1", "start", "开始");
//            FlowNode taskNode = createNode("task1", "task", "任务");
//            FlowNode endNode = createNode("end1", "end", "结束");
//            data.setNodes(Arrays.asList(startNode, taskNode, endNode));
//            data.setEdges(Collections.emptyList());
//
//            String bpmnXml = adapter.adapt(data);
//            assertNotNull(bpmnXml);
//            assertTrue(bpmnXml.contains("<userTask id=\"task1\""));
//        }
//
//        @Test
//        @DisplayName("测试流程ID和名称自定义")
//        void testCustomProcessIdAndName() {
//            FlowGraphData data = new FlowGraphData();
//            Map<String, Object> startProps = new HashMap<>();
//            startProps.put("processId", "customProcessId");
//            startProps.put("processName", "自定义流程名称");
//            FlowNode startNode = createNode("start1", "start", "开始", startProps);
//            FlowNode taskNode = createNode("task1", "task", "任务");
//            FlowNode endNode = createNode("end1", "end", "结束");
//            data.setNodes(Arrays.asList(startNode, taskNode, endNode));
//            data.setEdges(Collections.emptyList());
//
//            String bpmnXml = adapter.adapt(data);
//            assertNotNull(bpmnXml);
//            assertTrue(bpmnXml.contains("id=\"customProcessId\""));
//            assertTrue(bpmnXml.contains("name=\"自定义流程名称\""));
//        }
//    }
//
//    // ========== 辅助方法 ==========
//
//    private FlowGraphData createBasicFlowGraphData() {
//        FlowGraphData data = new FlowGraphData();
//
//        FlowNode startNode = createNode("start1", "start", "开始");
//        FlowNode taskNode = createNode("task1", "task", "审批任务",
//            Map.of("assignee", "${assignee}"));
//        FlowNode endNode = createNode("end1", "end", "结束");
//
//        data.setNodes(Arrays.asList(startNode, taskNode, endNode));
//
//        FlowEdge edge1 = new FlowEdge();
//        edge1.setId("edge1");
//        edge1.setSourceNodeId("start1");
//        edge1.setTargetNodeId("task1");
//
//        FlowEdge edge2 = new FlowEdge();
//        edge2.setId("edge2");
//        edge2.setSourceNodeId("task1");
//        edge2.setTargetNodeId("end1");
//
//        data.setEdges(Arrays.asList(edge1, edge2));
//
//        return data;
//    }
//
//    private FlowGraphData createFlowGraphDataWithNode(String nodeType, String nodeName) {
//        return createFlowGraphDataWithNode(nodeType, nodeName, null);
//    }
//
//    private FlowGraphData createFlowGraphDataWithNode(String nodeType, String nodeName, Map<String, Object> properties) {
//        FlowGraphData data = new FlowGraphData();
//
//        FlowNode startNode = createNode("start1", "start", "开始");
//        FlowNode targetNode = createNode("target1", nodeType, nodeName, properties);
//        FlowNode endNode = createNode("end1", "end", "结束");
//
//        data.setNodes(Arrays.asList(startNode, targetNode, endNode));
//        data.setEdges(Collections.emptyList());
//
//        return data;
//    }
//
//    private FlowNode createNode(String id, String type, String text) {
//        return createNode(id, type, text, null);
//    }
//
//    private FlowNode createNode(String id, String type, String text, Map<String, Object> properties) {
//        FlowNode node = new FlowNode();
//        node.setId(id);
//        node.setType(type);
//        node.setText(text);
//        node.setX(100);
//        node.setY(200);
//        if (properties != null) {
//            node.setProperties(properties);
//        }
//        return node;
//    }
//}