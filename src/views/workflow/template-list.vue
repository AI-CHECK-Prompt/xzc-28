<template>
  <div class="template-list">
    <el-table :data="templates" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="模板名称"></el-table-column>
      <el-table-column prop="category" label="类别"></el-table-column>
      <el-table-column prop="currentVersion" label="版本" width="80"></el-table-column>
      <el-table-column prop="updatedAt" label="更新时间"></el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button size="mini" @click="editTemplate(scope.row)">编辑</el-button>
          <el-button size="mini" type="primary" @click="openDesigner(scope.row)">设计流程</el-button>
          <el-button size="mini" type="danger" @click="disableTemplate(scope.row)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-button type="primary" @click="createTemplate">新建模板</el-button>
  </div>
</template>

<script>
export default {
  name: 'TemplateList',
  data() {
    return {
      templates: []
    };
  },
  mounted() {
    this.loadTemplates();
  },
  methods: {
    loadTemplates() {
      this.$http.get('/api/workflow/templates').then(res => {
        this.templates = res.data;
      });
    },
    editTemplate(row) {
      this.$message.info('编辑模板');
    },
    openDesigner(row) {
      this.$router.push(`/workflow/designer/${row.currentVersionId}`);
    },
    disableTemplate(row) {
      this.$message.info('禁用模板');
    },
    createTemplate() {
      this.$message.info('创建模板');
    }
  }
};
</script>