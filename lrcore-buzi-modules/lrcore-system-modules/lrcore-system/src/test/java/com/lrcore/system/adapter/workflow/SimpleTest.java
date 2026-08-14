//package com.lrcore.system.adapter.workflow;
//
//import com.lrcore.system.domain.workflow.definition.FlowEdge;
//import com.lrcore.system.domain.workflow.definition.FlowGraphData;
//import com.lrcore.system.domain.workflow.definition.FlowNode;
//
//import java.util.*;
//
/// **
// * 简单测试类，用于验证LogicFlow到BPMN转换功能
// * 不依赖Spring容器，可独立运行
// */
//public class SimpleTest {
//
//    public static void main(String[] args) {
//        System.out.println("=== LogicFlow到Flowable BPMN转换测试 ===\n");
//
//        // 创建适配器实例
//        LogicFlowToFlowableBpmnAdapter adapter = new LogicFlowToFlowableBpmnAdapter();
//
//        // 测试数据
//        FlowGraphData flowGraphData = createTestData();
//
//        // 测试验证功能
//        System.out.println("1. 测试数据验证...");
//        boolean isValid = adapter.validate(flowGraphData);
//        System.out.println("   验证结果: " + (isValid ? "通过" : "失败"));
//
//        // 测试转换功能
//        System.out.println("\n2. 测试BPMN转换...");
//        try {
//            String bpmnXml = adapter.adapt(flowGraphData);
//            System.out.println("   转换成功！");
//            System.out.println("   生成的BPMN XML长度: " + bpmnXml.length() + " 字符");
//            System.out.println("\n3. 生成的BPMN XML:");
//            System.out.println("====================================");
//            System.out.println(bpmnXml);
//            System.out.println("====================================");
//
//            // 验证XML内容
//            System.out.println("\n4. XML内容验证:");
//            verifyXmlContent(bpmnXml);
//
//        } catch (Exception e) {
//            System.out.println("   转换失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        // 测试支持的节点类型
//        System.out.println("\n5. 支持的节点类型:");
//        String[] nodeTypes = adapter.getSupportedNodeTypes();
//        for (String nodeType : nodeTypes) {
//            System.out.println("   - " + nodeType);
//        }
//
//        // 测试支持的连线类型
//        System.out.println("\n6. 支持的连线类型:");
//        String[] edgeTypes = adapter.getSupportedEdgeTypes();
//        for (String edgeType : edgeTypes) {
//            System.out.println("   - " + edgeType);
//        }
//
//        // 测试适配器信息
//        System.out.println("\n7. 适配器信息:");
//        System.out.println("   名称: " + adapter.getName());
//        System.out.println("   版本: " + adapter.getVersion());
//
//        // 测试边界条件
//        System.out.println("\n8. 边界条件测试:");
//        testBoundaryConditions(adapter);
//
//        System.out.println("\n=== 测试完成 ===");
//    }
//
//    /**
//     * 验证XML内容
//     */
//    private static void verifyXmlContent(String bpmnXml) {
//        check(bpmnXml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?"), "XML声明");
//        check(bpmnXml.contains("<definitions"), "definitions元素");
//        check(bpmnXml.contains("<process"), "process元素");
//        check(bpmnXml.contains("<startEvent"), "startEvent元素");
//        check(bpmnXml.contains("<userTask"), "userTask元素");
//        check(bpmnXml.contains("<exclusiveGateway"), "exclusiveGateway元素");
//        check(bpmnXml.contains("<endEvent"), "endEvent元素");
//        check(bpmnXml.contains("<sequenceFlow"), "sequenceFlow元素");
//        check(bpmnXml.contains("<conditionExpression"), "conditionExpression元素");
//        check(bpmnXml.contains("${approved == true}"), "条件表达式内容");
//    }
//
//    /**
//     * 检查条件
//     */
//    private static void check(boolean condition, String description) {
//        System.out.println("   " + (condition ? "✓" : "✗") + " " + description);
//    }
//
//    /**
//     * 测试边界条件
//     */
//    private static void testBoundaryConditions(LogicFlowToFlowableBpmnAdapter adapter) {
//        // 测试null数据
//        System.out.println("   null数据验证: " + (!adapter.validate(null) ? "✓通过" : "✗失败"));
//
//        // 测试空节点列表
//        FlowGraphData emptyData = new FlowGraphData();
//        emptyData.setNodes(Collections.emptyList());
//        System.out.println("   空节点列表验证: " + (!adapter.validate(emptyData) ? "✓通过" : "✗失败"));
//
//        // 测试无开始节点
//        FlowGraphData noStartData = new FlowGraphData();
//        FlowNode taskNode = new FlowNode();
//        taskNode.setId("task1");
//        taskNode.setType("task");
//        taskNode.setText("任务");
//        noStartData.setNodes(Arrays.asList(taskNode));
//        System.out.println("   无开始节点验证: " + (!adapter.validate(noStartData) ? "✓通过" : "✗失败"));
//
//        // 测试无效节点类型
//        FlowGraphData invalidTypeData = new FlowGraphData();
//        FlowNode startNode = new FlowNode();
//        startNode.setId("start1");
//        startNode.setType("start");
//        startNode.setText("开始");
//        FlowNode invalidNode = new FlowNode();
//        invalidNode.setId("invalid1");
//        invalidNode.setType("invalidType");
//        invalidNode.setText("无效类型");
//        FlowNode endNode = new FlowNode();
//        endNode.setId("end1");
//        endNode.setType("end");
//        endNode.setText("结束");
//        invalidTypeData.setNodes(Arrays.asList(startNode, invalidNode, endNode));
//        System.out.println("   无效节点类型验证: " + (!adapter.validate(invalidTypeData) ? "✓通过" : "✗失败"));
//
//        // 测试有效数据
//        FlowGraphData validData = createMinimalValidData();
//        System.out.println("   有效数据验证: " + (adapter.validate(validData) ? "✓通过" : "✗失败"));
//    }
//
//    /**
//     * 创建最小有效数据
//     */
//    private static FlowGraphData createMinimalValidData() {
//        FlowGraphData data = new FlowGraphData();
//
//        FlowNode startNode = new FlowNode();
//        startNode.setId("start1");
//        startNode.setType("start");
//        startNode.setText("开始");
//
//        FlowNode endNode = new FlowNode();
//        endNode.setId("end1");
//        endNode.setType("end");
//        endNode.setText("结束");
//
//        data.setNodes(Arrays.asList(startNode, endNode));
//        data.setEdges(Collections.emptyList());
//
//        return data;
//    }
//
//    /**
//     * 创建测试数据
//     */
//    private static FlowGraphData createTestData() {
//        FlowGraphData flowGraphData = new FlowGraphData();
//
//        // 创建开始节点
//        FlowNode startNode = new FlowNode();
//        startNode.setId("start1");
//        startNode.setType("start");
//        startNode.setText("开始");
//        startNode.setX(100);
//        startNode.setY(200);
//
//        // 创建用户任务节点
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
//        // 创建排他网关
//        FlowNode gatewayNode = new FlowNode();
//        gatewayNode.setId("gateway1");
//        gatewayNode.setType("exclusiveGateway");
//        gatewayNode.setText("审批结果");
//        gatewayNode.setX(500);
//        gatewayNode.setY(200);
//
//        // 创建结束节点
//        FlowNode endNode = new FlowNode();
//        endNode.setId("end1");
//        endNode.setType("end");
//        endNode.setText("结束");
//        endNode.setX(700);
//        endNode.setY(200);
//
//        List<FlowNode> nodes = Arrays.asList(startNode, taskNode, gatewayNode, endNode);
//        flowGraphData.setNodes(nodes);
//
//        // 创建连线
//        FlowEdge edge1 = new FlowEdge();
//        edge1.setId("edge1");
//        edge1.setSourceNodeId("start1");
//        edge1.setTargetNodeId("task1");
//        edge1.setText("开始到任务");
//
//        FlowEdge edge2 = new FlowEdge();
//        edge2.setId("edge2");
//        edge2.setSourceNodeId("task1");
//        edge2.setTargetNodeId("gateway1");
//        edge2.setText("任务到网关");
//
//        FlowEdge edge3 = new FlowEdge();
//        edge3.setId("edge3");
//        edge3.setSourceNodeId("gateway1");
//        edge3.setTargetNodeId("end1");
//        edge3.setText("网关到结束");
//        Map<String, Object> edgeProperties = new HashMap<>();
//        edgeProperties.put("conditionExpression", "${approved == true}");
//        edge3.setProperties(edgeProperties);
//
//        List<FlowEdge> edges = Arrays.asList(edge1, edge2, edge3);
//        flowGraphData.setEdges(edges);
//
//        return flowGraphData;
//    }
//}