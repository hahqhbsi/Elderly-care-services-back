
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 服务信息
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/fuwu")
public class FuwuController {
    private static final Logger logger = LoggerFactory.getLogger(FuwuController.class);

    @Autowired
    private FuwuService fuwuService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service

    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        System.out.println("--------------展示页面数据--------------");
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = fuwuService.queryPage(params);

        //字典表数据转换
        List<FuwuView> list =(List<FuwuView>)page.getList();
        for(FuwuView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        FuwuEntity fuwu = fuwuService.selectById(id);
        if(fuwu !=null){
            //entity转view
            FuwuView view = new FuwuView();
            BeanUtils.copyProperties( fuwu , view );//把实体数据重构到view中

            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            System.out.println("-------我在查询服务信息，服务详情-----："+R.ok().put("data", view));
            System.out.println("我用这个查询："+id);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody FuwuEntity fuwu, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,fuwu:{}",this.getClass().getName(),fuwu.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<FuwuEntity> queryWrapper = new EntityWrapper<FuwuEntity>()
            .eq("fuwu_bianhao", fuwu.getFuwuBianhao())
            .eq("fuwu_name", fuwu.getFuwuName())
            .eq("fuwu_types", fuwu.getFuwuTypes())
            ;



        FuwuEntity fuwuEntity = fuwuService.selectOne(queryWrapper);
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
       //这三项不能相同否则算错误
        if(fuwuEntity==null){
            fuwu.setCreateTime(new Date());
            fuwuService.insert(fuwu);
            System.out.println("---------------成功保存服务--------------");
            //System.out.println("收到："+fuwu);
           // System.out.println("返回："+R.ok());
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody FuwuEntity fuwu, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,fuwu:{}",this.getClass().getName(),fuwu.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        //根据字段查询是否有相同数据
        Wrapper<FuwuEntity> queryWrapper = new EntityWrapper<FuwuEntity>()
            .notIn("id",fuwu.getId())
            .andNew()
            .eq("fuwu_bianhao", fuwu.getFuwuBianhao())
            .eq("fuwu_name", fuwu.getFuwuName())
            .eq("fuwu_types", fuwu.getFuwuTypes())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        FuwuEntity fuwuEntity = fuwuService.selectOne(queryWrapper);
        if("".equals(fuwu.getFuwuPhoto()) || "null".equals(fuwu.getFuwuPhoto())){
                fuwu.setFuwuPhoto(null);
        }
        if(fuwuEntity==null){
            fuwuService.updateById(fuwu);//根据id更新
            System.out.println("------------成功更新服务信息--------------");
          //  System.out.println("收到："+fuwu);
          //  System.out.println("返回："+R.ok());
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        fuwuService.deleteBatchIds(Arrays.asList(ids));
        System.out.println("----------成功删除服务信息------------");
      //  System.out.println("收到："+ids);
      //  System.out.println("返回："+R.ok());
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        try {
            List<FuwuEntity> fuwuList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            FuwuEntity fuwuEntity = new FuwuEntity();
//                            fuwuEntity.setFuwuBianhao(data.get(0));                    //服务编号 要改的
//                            fuwuEntity.setFuwuName(data.get(0));                    //服务名称 要改的
//                            fuwuEntity.setFuwuPhoto("");//照片
//                            fuwuEntity.setFuwuTypes(Integer.valueOf(data.get(0)));   //服务类型 要改的
//                            fuwuEntity.setFuwuContent("");//照片
//                            fuwuEntity.setCreateTime(date);//时间
                            fuwuList.add(fuwuEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        fuwuService.insertBatch(fuwuList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }






}
