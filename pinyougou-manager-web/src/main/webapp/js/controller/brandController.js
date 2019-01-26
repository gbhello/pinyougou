//控制层
app.controller('brandController', function($scope,$controller, brandService) {

	$controller('baseController',{$scope:$scope});//继承
	
	// 读取列表数据绑定到表单中
	$scope.findAll = function() {
		brandService.findAll().success(function(response) {
			$scope.list = response;
		});
	}

	// 分页
	$scope.findPage = function(page, rows) {
		brandService.findPage(page, rows).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	// 保存
	$scope.save = function() {
		var serviceObject;// 服务层对象
		if ($scope.entity.id != null) {// 如果触发save方法的请求中含有id值，则是修改方法
			serviceObject = brandService.update($scope.entity);
		} else {
			serviceObject = brandService.add($scope.entity);// 增加
		}
		serviceObject.success(function(response) {
			if (response.success) {
				// 重新查询，刷新页面信息
				$scope.reloadList();
			} else {
				alert(response.message);
			}
		});
	}

	// 查询实体，用于修改功能信息回显
	$scope.findOne = function(id) {
		brandService.findOne(id).success(function(response) {
			$scope.entity = response;
		});
	}

	// 批量删除
	$scope.dele = function() {
		// 获取选中的复选框
		brandService.dele($scope.selectIds).success(function(response) {
			if (response.success) {
				$scope.reloadList();// 刷新列表
				$scope.selectIds = [];
			} else {
				alert(response.message);
			}
		});
	}

	// 定义搜索对象
	$scope.searchEntity = {};
	// 条件查询
	$scope.search = function(page, rows) {
		brandService.search(page, rows, $scope.searchEntity).success(
				function(response) {
					$scope.paginationConf.totalItems = response.total;// 总记录数
					$scope.list = response.rows;// 给列表变量赋值
				});
	}
});