app.controller('searchController',function($scope,searchService){
	//搜索
	$scope.search=function(){
		searchService.search($scope.searchMap).success(
				function(response){
					$scope.resultMap=response;//搜索返回的结果
				}
		);
	}
	
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{}};//搜索对象
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand'){	//如果帝丹吉的使分欸或是品牌
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}
	}
	
	//移出复合搜索条件
	$scope.removeSearchItem=function(key){
		if(key=="category" || key=="brand"){//如果是分类或品牌
			$scope.searchMap[key]="";
		}else{
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//执行搜索
	}
	
	//添加复合搜索条件
	$scope.addSearchItem=function(key,value){
		if(key=="category" || key=="brand"){//如果是分类或品牌
			$scope.searchMap[key]=value;
			}else{
				$scope.searchMap.spec[key]=value;
			}
		$scope.search();//执行搜索
	}
});