<template>
  <div class="instance-list">
    <el-table :data="instances" style="width: 100%">
      <el-table-column prop="id" label="实例ID" width="80"></el-table-column>
      <el-table-column prop="contractName" label="合同名称"></el-table-column>
      <el-table-column prop="templateName" label="模板名称"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="currentNodeName" label="当前节点"></el-table-column>
      <el-table-column prop="startedAt" label="启动时间"></el-table-column>
      <el-table-column label="操作" width="150">
        <template slot-scope="scope">
          <el-button size="mini" @click="viewDetail(scope.row)">详情</el-button>
          <el-button size="mini" type="primary" @click="viewLogs(scope.row)">日志</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'InstanceList',
  data() {
    return {
      instances: []
    };
  },
  mounted() {
    this.loadInstances();
  },
  methods: {
    loadInstances() {
      this.$http.get('/api/workflow/instances').then(res => {
        this.instances = res.data;
      });
    },
    getStatusType(status) {
      const typeMap = {
        'PENDING': 'info',
        'IN_PROGRESS': 'warning',
        'COMPLETED': 'success',
        'CANCELLED': 'danger',
        'TIMEOUT': 'warning'
      };
      return typeMap[status] || 'info';
    },
    viewDetail(row) {
      this.$router.push(`/workflow/instances/${row.id}`);
    },
    viewLogs(row) {
      this.$router.push(`/workflow/instances/${row.id}/logs`);
    }
  }
};
</script>