<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="用户id" prop="userId">
      <el-input v-model="dataForm.userId" placeholder="用户id"></el-input>
    </el-form-item>
    <el-form-item label="sku_id" prop="skuId">
      <el-input v-model="dataForm.skuId" placeholder="sku_id"></el-input>
    </el-form-item>
    <el-form-item label="sku标题" prop="skuTitle">
      <el-input v-model="dataForm.skuTitle" placeholder="sku标题"></el-input>
    </el-form-item>
    <el-form-item label="sku默认图片" prop="skuImage">
      <el-input v-model="dataForm.skuImage" placeholder="sku默认图片"></el-input>
    </el-form-item>
    <el-form-item label="创建时间" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="创建时间"></el-input>
    </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        dataForm: {
          id: 0,
          userId: '',
          skuId: '',
          skuTitle: '',
          skuImage: '',
          createTime: ''
        },
        dataRule: {
          userId: [
            { required: true, message: '用户id不能为空', trigger: 'blur' }
          ],
          skuId: [
            { required: true, message: 'sku_id不能为空', trigger: 'blur' }
          ],
          skuTitle: [
            { required: true, message: 'sku标题不能为空', trigger: 'blur' }
          ],
          skuImage: [
            { required: true, message: 'sku默认图片不能为空', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: '创建时间不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/ums/usercollectsku/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.userId = data.userCollectSku.userId
                this.dataForm.skuId = data.userCollectSku.skuId
                this.dataForm.skuTitle = data.userCollectSku.skuTitle
                this.dataForm.skuImage = data.userCollectSku.skuImage
                this.dataForm.createTime = data.userCollectSku.createTime
              }
            })
          }
        })
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/ums/usercollectsku/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'userId': this.dataForm.userId,
                'skuId': this.dataForm.skuId,
                'skuTitle': this.dataForm.skuTitle,
                'skuImage': this.dataForm.skuImage,
                'createTime': this.dataForm.createTime
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: '操作成功',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>
