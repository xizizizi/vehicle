<template>
  <div id="digital-flop">
    <div
      class="digital-flop-item"
      v-for="item in digitalFlopData"
      :key="item.title"
    >
      <div class="digital-flop-title">{{ item.title }}</div>
      <div class="digital-flop">
        <dv-digital-flop
          :config="item.number"
          style="width: 100px; height: 50px"
        />
        <div class="unit">{{ item.unit }}</div>
      </div>
    </div>

    <dv-decoration-10 />
  </div>
</template>
  
  <script>
export default {
  name: "DigitalFlop",
  data() {
    return {
      digitalFlopData: [],
      areasData: [], // 新增数组来存储从后端获取的区域数据
    };
  },
  methods: {
    // API请求函数，获取数据
    async fetchData() {
      try {
        const response = await fetch("http://localhost:8080/api/areas");
        const data = await response.json();
        this.areasData = data;
        this.createData();
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    },

    // 更新digitalFlopData的方法，基于后端数据进行处理
    createData() {
      const areaTotals = {
        BRIDGE: 0,
        TUNNEL: 0,
        ROAD: 0,
        COMMERCIAL: 0,
        RESIDENTIAL: 0,
        INDUSTRIAL: 0,
        NATURAL: 0,
        SPECIAL: 0,
      };

      // 对每个区域进行累加
      this.areasData.forEach((item) => {
        const { type, baseValue, unit } = item;

        // 累加每个区域类型的面积
        if (areaTotals[type] !== undefined) {
          areaTotals[type] += baseValue;
        }
      });

      // 将累加后的数据填充到digitalFlopData
      this.digitalFlopData = [
        {
          title: "覆盖区域面积",
          number: {
            number: [
              areaTotals.COMMERCIAL +
                areaTotals.RESIDENTIAL +
                areaTotals.INDUSTRIAL +
                areaTotals.NATURAL +
                areaTotals.SPECIAL,
            ],
            content: "{nt}",
            textAlign: "right",
            style: {
              fill: "#4d99fc",
              fontWeight: "bold",
            },
          },
          unit: "平方公里",
        },
        {
          title: "桥梁",
          number: {
            number: [areaTotals.BRIDGE],
            content: "{nt}",
            textAlign: "right",
            style: {
              fill: "#f46827",
              fontWeight: "bold",
            },
          },
          unit: "座",
        },
        {
          title: "涵洞隧道",
          number: {
            number: [areaTotals.TUNNEL],
            content: "{nt}",
            textAlign: "right",
            style: {
              fill: "#40faee",
              fontWeight: "bold",
            },
          },
          unit: "个",
        },
        {
          title: "交通干道",
          number: {
            number: [areaTotals.ROAD],
            content: "{nt}",
            textAlign: "right",
            style: {
              fill: "#4d99fc",
              fontWeight: "bold",
            },
          },
          unit: "条",
        },
        {
          title: "商业区",
          number: {
            number: [areaTotals.COMMERCIAL],
            content: "{nt}",
            textAlign: "right",
            style: {
              fill: "#f46827",
              fontWeight: "bold",
            },
          },
          unit: "公顷",
        },
        {
          title: "住宅区",
          number: {
            number: [areaTotals.RESIDENTIAL],
            content: "{nt}",
            textAlign: "right",
            style: {
              fill: "#40faee",
              fontWeight: "bold",
            },
          },
          unit: "公顷",
        },
        {
          title: "工业区",
          number: {
            number: [areaTotals.INDUSTRIAL],
            content: "{nt}",
            textAlign: "right",
            style: {
              fill: "#4d99fc",
              fontWeight: "bold",
            },
          },
          unit: "公顷",
        },
        {
          title: "自然区域",
          number: {
            number: [areaTotals.NATURAL],
            content: "{nt}",
            textAlign: "right",
            style: {
              fill: "#f46827",
              fontWeight: "bold",
            },
          },
          unit: "公顷",
        },
        {
          title: "特殊区域",
          number: {
            number: [areaTotals.SPECIAL],
            content: "{nt}",
            textAlign: "right",
            style: {
              fill: "#40faee",
              fontWeight: "bold",
            },
          },
          unit: "公顷",
        },
      ];
    },
  },
  mounted() {
    this.fetchData(); // 页面加载时请求数据

    // 设置每300秒重新获取一次数据
    setInterval(this.fetchData, 300000);
  },
};
</script>
  
  <style lang="scss" scoped>
#digital-flop {
  position: relative;
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  background-color: rgba(6, 30, 93, 0.5);
  padding: 10px;
  gap: 8px;

  .dv-decoration-10 {
    position: absolute;
    width: 95%;
    left: 2.5%;
    bottom: 0;
    height: 5px;
  }

  .digital-flop-item {
    flex: 1 1 100px; /* 最小100px，均分空间 */
    min-width: 80px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 5px 0;
  }

  .digital-flop-title {
    font-size: 16px;
    margin-bottom: 8px;
    white-space: nowrap;
  }

  .digital-flop {
    display: flex;
    align-items: baseline;
    justify-content: center;
    flex-wrap: wrap;
  }

  .unit {
    margin-left: 5px;
    font-size: 14px;
  }
}

@media (max-width: 1200px) {
  #digital-flop .digital-flop-title {
    font-size: 14px;
  }
}
@media (max-width: 768px) {
  #digital-flop .digital-flop-item {
    flex-basis: 50%;
  }
}
</style>