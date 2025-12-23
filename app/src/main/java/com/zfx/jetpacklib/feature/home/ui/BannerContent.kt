package com.zfx.jetpacklib.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zfx.jetpacklib.R
import com.zfx.jetpacklib.data.BannerItem


@Composable
fun BannerContent(
    modifier: Modifier = Modifier.fillMaxHeight().fillMaxWidth(),
    pageData : BannerItem
){
    Box(modifier = modifier.fillMaxSize()){
        AsyncImage(
            model = pageData.imagePath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.BottomCenter)
                .background(colorResource(id =  R.color.color_808A8A8A))
        ){
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)  // 垂直居中，水平靠左
                    .padding(start = 16.dp),  // 左右间距
                text = pageData.title.orEmpty(),
                fontSize = 14.sp,
                color = colorResource(id =  R.color.white)
            )
        }

    }
}


@Preview
@Composable
fun BannerItemPreview(){
    BannerContent(
        pageData = BannerItem(
            imagePath = "https://www.wanandroid.com/blogimgs/62c1bd68-b5f3-4a3c-a649-7ca8c7dfabe6.png",
            title = "我们新增了一个常用导航Tab~",
            url = "https://www.wanandroid.com/navi"
        )
    )
}