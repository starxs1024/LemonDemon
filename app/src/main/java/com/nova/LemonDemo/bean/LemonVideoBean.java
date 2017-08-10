package com.nova.LemonDemo.bean;

import java.util.List;

/**
 * Created by Paraselene on 2017/7/28.
 * Email ：15616165649@163.com
 */

public class LemonVideoBean {

    /**
     * pageCount : 148
     * sublist : [{"v_id":11,"v_imagelinks":"http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/15877550_703523806488035_2866914275220258816_n.jpg","v_time":"00:11","v_title":"لا تلعب الشعوذة عندما أجلس معك!","v_videolink":"http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/16125709_173812393101033_2871721073768923136_n.mp4"},{"v_id":12,"v_imagelinks":"http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/16583292_1227294984053182_3418037482141253632_n.jpg","v_time":"00:40","v_title":"حمير آخر موديل","v_videolink":"http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/16755183_996335110501181_5675960536211128320_n.mp4"},{"v_id":13,"v_imagelinks":"http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/16789766_1452333434779915_405454035252936704_n.jpg","v_time":"00:29","v_title":"يا ما تحت السواهي دواهي","v_videolink":"http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/16936780_1235015626614037_8085933837810401280_n.mp4"},{"v_id":14,"v_imagelinks":"http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/10424650_670986332949880_430078745_n.jpg","v_time":"00:14","v_title":"المغني في الشارع","v_videolink":"http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/10417942_240530986155043_348949671_n.mp4"},{"v_id":15,"v_imagelinks":"http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/16464371_1721653924811645_3662504284735406080_n.jpg","v_time":"00:09","v_title":"أقوى منفاخ","v_videolink":"http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/16732812_1800989246892780_5724526458731233280_n.mp4"},{"v_id":16,"v_imagelinks":"http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/13423691_580018822179717_1095665197_n.jpg","v_time":"00:15","v_title":"مجلس خطر جدا!","v_videolink":"http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/13408468_1204534426244873_1983607754_n.mp4"},{"v_id":17,"v_imagelinks":"http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/16465305_767288880089690_283721360570580992_n.jpg","v_time":"00:07","v_title":"أخي مصاب بالكرة","v_videolink":"http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/16471247_402664406753569_3218705911040180224_n.mp4"},{"v_id":18,"v_imagelinks":"http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/16906811_199870387158161_653701066197565440_n.jpg","v_time":"00:10","v_title":"سيارة جديد في الشارع","v_videolink":"http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/16913774_158784237967112_3232202566594985984_n.mp4"},{"v_id":19,"v_imagelinks":"http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/16788454_730287090470381_2541523816253751296_n.jpg","v_time":"00:59","v_title":"أسرع مواطن سعودي","v_videolink":"http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/16784143_303747426706796_4380279970862202880_n.mp4"},{"v_id":20,"v_imagelinks":"http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/16583287_1658188861141660_2377777105761992704_n.jpg","v_time":"00:38","v_title":"موضه جديده","v_videolink":"http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/16783517_1228891770528351_2175922559202099200_n.mp4"}]
     */

    private int pageCount;
    private List<SublistBean> sublist;

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public List<SublistBean> getSublist() {
        return sublist;
    }

    public void setSublist(List<SublistBean> sublist) {
        this.sublist = sublist;
    }

    public static class SublistBean {
        /**
         * v_id : 11
         * v_imagelinks : http://d90mtt9ugu6z7.cloudfront.net/prod/t51.2885-15/e15/15877550_703523806488035_2866914275220258816_n.jpg
         * v_time : 00:11
         * v_title : لا تلعب الشعوذة عندما أجلس معك!
         * v_videolink : http://d90mtt9ugu6z7.cloudfront.net/prod/t50.2886-16/16125709_173812393101033_2871721073768923136_n.mp4
         */

        private int v_id;
        private String v_imagelinks;
        private String v_time;
        private String v_title;
        private String v_videolink;

        public int getV_id() {
            return v_id;
        }

        public void setV_id(int v_id) {
            this.v_id = v_id;
        }

        public String getV_imagelinks() {
            return v_imagelinks;
        }

        public void setV_imagelinks(String v_imagelinks) {
            this.v_imagelinks = v_imagelinks;
        }

        public String getV_time() {
            return v_time;
        }

        public void setV_time(String v_time) {
            this.v_time = v_time;
        }

        public String getV_title() {
            return v_title;
        }

        public void setV_title(String v_title) {
            this.v_title = v_title;
        }

        public String getV_videolink() {
            return v_videolink;
        }

        public void setV_videolink(String v_videolink) {
            this.v_videolink = v_videolink;
        }
    }
}
