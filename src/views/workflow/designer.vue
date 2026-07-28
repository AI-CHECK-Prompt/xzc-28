<template>
  <div class="workflow-designer">
    <el-container>
      <el-aside width="200px">
        <el-tree :data="nodeTypes" @node-click="addNode">
          <span slot-scope="{ data }">
            <i :class="data.icon"></i> {{ data.label }}
          </span>
        </el-tree>
      </el-aside>
      <el-main>
        <div ref="canvas" class="bpmn-canvas"></div>
      </el-main>
      <el-aside width="300px">
        <el-form v-if="selectedNode" :model="selectedNode" label-width="120px">
          <el-form-item label="节点名称">
            <el-input v-model="selectedNode.nodeName"></el-input>
          </el-form-item>
          <el-form-item label="节点类型">
            <el-select v-model="selectedNode.nodeType">
              <el-option label="签署节点" value="SIGN_SIGNER"></el-option>
              <el-option label="条件分支" value="CONDITION"></el-option>
              <el-option label="并行签署组" value="PARALLEL_GROUP"></el-option>
              <el-option label="超时处理" value="TIMEOUT"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="签署方" v-if="selectedNode.nodeType === 'SIGN_SIGNER'">
            <el-select v-model="selectedNode.config.signerId" placeholder="选择签署方">
              <el-option v-for="s in signers" :key="s.id" :label="s.name" :value="s.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="签署方式" v-if="selectedNode.nodeType === 'SIGN_SIGNER'">
            <el-select v-model="selectedNode.config.signMethod">
              <el-option label="顺序签署" value="SEQUENTIAL"></el-option>
              <el-option label="并行签署" value="PARALLEL"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="顺序权重" v-if="selectedNode.nodeType === 'SIGN_SIGNER'">
            <el-input-number v-model="selectedNode.config.orderWeight" :min="1"></el-input-number>
          </el-form-item>
          <el-form-item label="超时时间(小时)">
            <el-input-number v-model="selectedNode.timeoutHours" :min="0"></el-input-number>
          </el-form-item>
          <el-form-item label="超时动作">
            <el-select v-model="selectedNode.timeoutAction">
              <el-option label="自动跳过" value="AUTO_SKIP"></el-option>
              <el-option label="发送提醒" value="REMIND"></el-option>
              <el-option label="升级处理" value="ESCALATE"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="条件表达式" v-if="selectedNode.nodeType === 'CONDITION'">
            <el-input v-model="selectedNode.config.conditionExpression" placeholder="如: signerCount > 3"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveNode">保存节点</el-button>
            <el-button type="danger" @click="deleteNode">删除节点</el-button>
          </el-form-item>
        </el-form>
      </el-aside>
    </el-container>
    <el-footer>
      <el-button @click="loadTemplate">加载模板</el-button>
      <el-button @click="saveTemplate">保存模板</el-button>
      <el-button type="success" @click="validateAndDeploy">验证并发布</el-button>
    </el-footer>
  </div>
</template>

<script>
import BpmnViewer from 'bpmn-js';
import CustomModeler from './CustomModeler';

export default {
  name: 'WorkflowDesigner',
  data() {
    return {
      viewer: null,
      modeler: null,
      selectedNode: null,
      signers: [],
      nodeTypes: [
        { label: '签署节点', icon: 'el-icon-user', type: 'SIGN_SIGNER' },
        { label: '条件分支', icon: 'el-icon-share', type: 'CONDITION' },
        { label: '并行签署组', icon: 'el-icon-more', type: 'PARALLEL_GROUP' },
        { label: '超时处理', icon: 'el-icon-time', type: 'TIMEOUT' },
        { label: '开始节点', icon: 'el-icon-video-play', type: 'START' },
        { label: '结束节点', icon: 'el-icon-video-pause', type: 'END' }
      ]
    };
  },
  mounted() {
    this.initBpmn();
    this.loadSigners();
  },
  methods: {
    initBpmn() {
      this.viewer = new BpmnViewer({ container: this.$refs.canvas });
      this.modeler = new CustomModeler({ container: this.$refs.canvas });
    },
    loadSigners() {
      // 从API加载签署方列表
      this.$http.get('/api/signers').then(res => {
        this.signers = res.data;
      });
    },
    addNode(data) {
      // 在画布上添加新节点
      this.modeler.addNode(data.type);
    },
    saveNode() {
      if (!this.selectedNode) return;
      this.$message.success('节点保存成功');
    },
    deleteNode() {
      this.$message.info('节点删除');
    },
    loadTemplate() {
      // 加载模板数据
    },
    saveTemplate() {
      // 保存模板
    },
    validateAndDeploy() {
      // 验证并发布
    }
  }
};
</script>

<style scoped>
.workflow-designer {
  height: 100%;
}
.bpmn-canvas {
  height: 500px;
  border: 1px solid #ddd;
}
</style>