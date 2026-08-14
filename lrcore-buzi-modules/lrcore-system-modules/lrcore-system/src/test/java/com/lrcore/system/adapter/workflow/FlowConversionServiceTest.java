//package com.lrcore.system.adapter.workflow;
//
//import com.lrcore.common.flowable.model.definition.FlowEdge;
//import com.lrcore.common.flowable.model.definition.FlowGraphData;
//import com.lrcore.common.flowable.model.definition.FlowNode;
//import com.lrcore.common.flowable.service.FlowConversionService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
/// **
// * FlowConversionService测试类
// */
//@SpringBootTest
//class FlowConversionServiceTest {
//
//    @Autowired
//    private FlowConversionService flowConversionService;
//
//    private FlowGraphData testFlowGraphData;
//
//    @BeforeEach
//    void setUp() {
//        testFlowGraphData = createTestFlowGraphData();
//    }
//
//    @Nested
//    @DisplayName("转换功能测试")
//    class ConversionTest {
//
//        @Test
//        @DisplayName("测试基本转换")
//        void testConvertToBpmnXml() {
//            String bpmnXml = flowConversionService.convertToBpmnXml(testFlowGraphData);
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
//        @DisplayName("测试null数据转换抛出异常")
//        void testConvertNullData() {
//            assertThrows(IllegalArgumentException.class,
//                    () -> flowConversionService.convertToBpmnXml(null));
//        }
//
//        @Test
//        @DisplayName("测试无效数据转换抛出异常")
//        void testConvertInvalidData() {
//            FlowGraphData invalidData = new FlowGraphData();
//            assertThrows(IllegalArgumentException.class,
//                    () -> flowConversionService.convertToBpmnXml(invalidData));
//        }
//
//        @Test
//        @DisplayName("测试转换结果长度")
//        void testConversionResultLength() {
//            String bpmnXml = flowConversionService.convertToBpmnXml(testFlowGraphData);
//            assertTrue(bpmnXml.length() > 100);
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
//            assertTrue(flowConversionService.validateFlowGraphData(testFlowGraphData));
//        }
//
//        @Test
//        @DisplayName("测试null数据验证返回false")
//        void testValidateNullData() {
//            assertFalse(flowConversionService.validateFlowGraphData(null));
//        }
//
//        @Test
//        @DisplayName("测试无开始节点数据验证返回false")
//        void testValidateNoStartNode() {
//            FlowGraphData noStartData = new FlowGraphData();
//            FlowNode taskNode = new FlowNode();
//            taskNode.setId("task1");
//            taskNode.setType("task");
//            taskNode.setText("任务");
//            FlowNode endNode = new FlowNode();
//            endNode.setId("end1");
//            endNode.setType("end");
//            endNode.setText("结束");
//            noStartData.setNodes(Arrays.asList(taskNode, endNode));
//            assertFalse(flowConversionService.validateFlowGraphData(noStartData));
//        }
//    }
//
//    @Nested
//    @DisplayName("类型查询测试")
//    class TypeQueryTest {
//
//        @Test
//        @DisplayName("测试获取支持的节点类型")
//        void testGetSupportedNodeTypes() {
//            String[] nodeTypes = flowConversionService.getSupportedNodeTypes();
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
//            String[] edgeTypes = flowConversionService.getSupportedEdgeTypes();
//            assertNotNull(edgeTypes);
//            assertTrue(edgeTypes.length >= 3);
//            assertTrue(Arrays.asList(edgeTypes).contains("sequenceFlow"));
//            assertTrue(Arrays.asList(edgeTypes).contains("conditionalFlow"));
//            assertTrue(Arrays.asList(edgeTypes).contains("defaultFlow"));
//        }
//    }
//
//    @Nested
//    @DisplayName("适配器信息测试")
//    class AdapterInfoTest {
//
//        @Test
//        @DisplayName("测试获取适配器名称")
//        void testGetAdapterName() {
//            String adapterName = flowConversionService.getAdapterName();
//            assertNotNull(adapterName);
//            assertEquals("LogicFlow to Flowable BPMN 2.0 Adapter", adapterName);
//        }
//
//        @Test
//        @DisplayName("测试获取适配器版本")
//        void testGetAdapterVersion() {
//            String adapterVersion = flowConversionService.getAdapterVersion();
//            assertNotNull(adapterVersion);
//            assertEquals("1.0.0", adapterVersion);
//        }
//    }
//
//    @Nested
//    @DisplayName("集成场景测试")
//    class IntegrationScenarioTest {
//
//        @Test
//        @DisplayName("测试带条件分支的流程转换")
//        void testProcessWithConditionBranches() {
//            FlowGraphData data = new FlowGraphData();
//
//            FlowNode startNode = createNode("start1", "start", "开始");
//            FlowNode taskNode = createNode("task1", "task", "审批",
//                    Map.of("assignee", "${assignee}"));
//            FlowNode gatewayNode = createNode("gateway1", "exclusiveGateway", "审批结果");
//            FlowNode hrNode = createNode("hr1", "userTask", "HR审批",
//                    Map.of("assignee", "${hr}"));
//            FlowNode endNode = createNode("end1", "end", "结束");
//
//            data.setNodes(Arrays.asList(startNode, taskNode, gatewayNode, hrNode, endNode));
//
//            FlowEdge edge1 = createEdge("edge1", "start1", "task1");
//            FlowEdge edge2 = createEdge("edge2", "task1", "gateway1");
//
//            Map<String, Object> passProps = new HashMap<>();
//            passProps.put("conditionExpression", "${approved == true}");
//            FlowEdge edge3 = createEdge("edge3", "gateway1", "hr1", passProps);
//
//            Map<String, Object> rejectProps = new HashMap<>();
//            rejectProps.put("conditionExpression", "${approved == false}");
//            FlowEdge edge4 = createEdge("edge4", "gateway1", "end1", rejectProps);
//
//            FlowEdge edge5 = createEdge("edge5", "hr1", "end1");
//
//            data.setEdges(Arrays.asList(edge1, edge2, edge3, edge4, edge5));
//
//            String bpmnXml = flowConversionService.convertToBpmnXml(data);
//
//            assertNotNull(bpmnXml);
//            assertTrue(bpmnXml.contains("<exclusiveGateway"));
//            assertTrue(bpmnXml.contains("<conditionExpression"));
//            assertTrue(bpmnXml.contains("${approved == true}"));
//            assertTrue(bpmnXml.contains("${approved == false}"));
//        }
//
//        @Test
//        @DisplayName("测试包含多种节点类型的流程转换")
//        void testProcessWithMultipleNodeTypes() {
//            FlowGraphData data = new FlowGraphData();
//
//            FlowNode startNode = createNode("start1", "start", "开始");
//            FlowNode userTaskNode = createNode("task1", "userTask", "用户任务",
//                    Map.of("assignee", "${user}"));
//            FlowNode serviceTaskNode = createNode("service1", "serviceTask", "服务任务",
//                    Map.of("class", "com.example.ServiceDelegate"));
//            FlowNode scriptTaskNode = createNode("script1", "scriptTask", "脚本任务",
//                    Map.of("scriptFormat", "javascript", "script", "print('test')"));
//            FlowNode endNode = createNode("end1", "end", "结束");
//
//            data.setNodes(Arrays.asList(startNode, userTaskNode, serviceTaskNode, scriptTaskNode, endNode));
//            data.setEdges(Collections.emptyList());
//
//            String bpmnXml = flowConversionService.convertToBpmnXml(data);
//
//            assertNotNull(bpmnXml);
//            assertTrue(bpmnXml.contains("<startEvent"));
//            assertTrue(bpmnXml.contains("<userTask"));
//            assertTrue(bpmnXml.contains("<serviceTask"));
//            assertTrue(bpmnXml.contains("<scriptTask"));
//            assertTrue(bpmnXml.contains("<endEvent"));
//        }
//    }
//
//    // ========== 辅助方法 ==========
//
//    private FlowGraphData createTestFlowGraphData() {
//        FlowGraphData flowGraphData = new FlowGraphData();
//
//        FlowNode startNode = new FlowNode();
//        startNode.setId("start1");
//        startNode.setType("start");
//        startNode.setText("开始");
//        startNode.setX(100);
//        startNode.setY(200);
//
//        FlowNode taskNode = new FlowNode();
//        taskNode.setId("task1");
//        taskNode.setType("task");
//        taskNode.setText("审批任务");
//        taskNode.setX(300);
//        taskNode.setY(200);
//        Map<String, Object> taskProperties = new HashMap<>();
//        taskProperties.put("assignee", "${assignee}");
//        taskNode.setProperties(taskProperties);
//
//        FlowNode endNode = new FlowNode();
//        endNode.setId("end1");
//        endNode.setType("end");
//        endNode.setText("结束");
//        endNode.setX(500);
//        endNode.setY(200);
//
//        flowGraphData.setNodes(Arrays.asList(startNode, taskNode, endNode));
//
//        FlowEdge edge1 = new FlowEdge();
//        edge1.setId("edge1");
//        edge1.setSourceNodeId("start1");
//        edge1.setTargetNodeId("task1");
//        edge1.setText("开始到任务");
//
//        FlowEdge edge2 = new FlowEdge();
//        edge2.setId("edge2");
//        edge2.setSourceNodeId("task1");
//        edge2.setTargetNodeId("end1");
//        edge2.setText("任务到结束");
//
//        flowGraphData.setEdges(Arrays.asList(edge1, edge2));
//
//        return flowGraphData;
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
//
//    private FlowEdge createEdge(String id, String sourceNodeId, String targetNodeId) {
//        return createEdge(id, sourceNodeId, targetNodeId, null);
//    }
//
//    private FlowEdge createEdge(String id, String sourceNodeId, String targetNodeId, Map<String, Object> properties) {
//        FlowEdge edge = new FlowEdge();
//        edge.setId(id);
//        edge.setSourceNodeId(sourceNodeId);
//        edge.setTargetNodeId(targetNodeId);
//        if (properties != null) {
//            edge.setProperties(properties);
//        }
//        return edge;
//    }
//}