app.controller('searchController',function($scope,$location,searchService){
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(
				function(response){
					$scope.resultMap=response;//搜索返回的结果
					buildPageLabel();//调用
				}
		);
	}
	
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sort':''};//搜索对象
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand' || key=='price'){	//如果关键字是分类或是品牌
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}
	}
	
	//移出复合搜索条件
	$scope.removeSearchItem=function(key){
		if(key=="category" || key=="brand" || key=="price"){//如果是分类或品牌
			$scope.searchMap[key]="";
		}else{
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//执行搜索
	}
	
	//添加复合搜索条件
	$scope.addSearchItem=function(key,value){
		if(key=="category" || key=="brand" || key=="price"){//如果是分类或品牌
			$scope.searchMap[key]=value;
			}else{
				$scope.searchMap.spec[key]=value;
			}
		$scope.search();//执行搜索
	}
	
	//构建分页标签
	buildPageLabel=function(){
		$scope.pageLabel=[];//新增分页栏属性
		var maxPageNo = $scope.resultMap.totalPages;//得到最后页码
		var firstPage=1;//开始页码
		var lastPage=maxPageNo;//截止页码
		
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后面有点
		
		if($scope.resultMap.totalPages>5){//如果总页数大于5页，显示部分页码
			if($scope.searchMap.pageNo<=3){//如果当前页小于等于3
				lastPage=5;//前5页
				
				$scope.firstDot=false;//前面没点
			}else if($scope.searchMap.pageNo>=lastPage-2){//如果当前页大于等于最大页码-2
				firstPage=maxPageNo-4;//后5页
				
				$scope.lastDot=false;//后面没点
			}else{//显示当前页中心的5页
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
		}else{
			$scope.firstDot=false;//前面无点
			$scope.lastDot=false;//后面五点
		}
		//循环产生页码标签
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		//页码验证
		if(pageNo<1||pageNo>$scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
	}
	
	//判断当前页是否为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	//判断当前页是否是最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
	
	//设置排序规则
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		$scope.search();
	}
	
	//判断关键字是不是品牌
	$scope.keywordsIsBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){//如果关键字是品牌
				return true;
			}
		}
		return false;
	}
	
	//接收参数并添加关键字
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords=$location.search()['keywords'];
		$scope.search();
	}
});