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
          style="width:100px;height:50px;"
        />
        <div class="unit">{{ item.unit }}</div>
      </div>
    </div>

    <dv-decoration-10 />
  </div>
</template>

<script>
export default {
  name: 'DigitalFlop',
  data () {
    return {
      digitalFlopData: [],
      areasData: [] // 新增数组来存储从后端获取的区域数据
    }
  },
  methods: {
    // API请求函数，获取数据
    async fetchData() {
      try {
        const response = await fetch('http://localhost:8080/api/areas');
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
      this.areasData.forEach(item => {
        const { type, baseValue, unit } = item;

        // 累加每个区域类型的面积
        if (areaTotals[type] !== undefined) {
          areaTotals[type] += baseValue;
        }
      });

      // 将累加后的数据填充到digitalFlopData
      this.digitalFlopData = [
        {
          title: '覆盖区域面积',
          number: {
            number: [ areaTotals.COMMERCIAL + areaTotals.RESIDENTIAL + areaTotals.INDUSTRIAL + areaTotals.NATURAL + areaTotals.SPECIAL],
            content: '{nt}',
            textAlign: 'right',
            style: {
              fill: '#4d99fc',
              fontWeight: 'bold'
            }
          },
          unit: '平方公里'
        },
        {
          title: '桥梁',
          number: {
            number: [areaTotals.BRIDGE],
            content: '{nt}',
            textAlign: 'right',
            style: {
              fill: '#f46827',
              fontWeight: 'bold'
            }
          },
          unit: '座'
        },
        {
          title: '涵洞隧道',
          number: {
            number: [areaTotals.TUNNEL],
            content: '{nt}',
            textAlign: 'right',
            style: {
              fill: '#40faee',
              fontWeight: 'bold'
            }
          },
          unit: '个'
        },
        {
          title: '交通干道',
          number: {
            number: [areaTotals.ROAD],
            content: '{nt}',
            textAlign: 'right',
            style: {
              fill: '#4d99fc',
              fontWeight: 'bold'
            }
          },
          unit: '条'
        },
        {
          title: '商业区',
          number: {
            number: [areaTotals.COMMERCIAL],
            content: '{nt}',
            textAlign: 'right',
            style: {
              fill: '#f46827',
              fontWeight: 'bold'
            }
          },
          unit: '公顷'
        },
        {
          title: '住宅区',
          number: {
            number: [areaTotals.RESIDENTIAL],
            content: '{nt}',
            textAlign: 'right',
            style: {
              fill: '#40faee',
              fontWeight: 'bold'
            }
          },
          unit: '公顷'
        },
        {
          title: '工业区',
          number: {
            number: [areaTotals.INDUSTRIAL],
            content: '{nt}',
            textAlign: 'right',
            style: {
              fill: '#4d99fc',
              fontWeight: 'bold'
            }
          },
          unit: '公顷'
        },
        {
          title: '自然区域',
          number: {
            number: [areaTotals.NATURAL],
            content: '{nt}',
            textAlign: 'right',
            style: {
              fill: '#f46827',
              fontWeight: 'bold'
            }
          },
          unit: '公顷'
        },
        {
          title: '特殊区域',
          number: {
            number: [areaTotals.SPECIAL],
            content: '{nt}',
            textAlign: 'right',
            style: {
              fill: '#40faee',
              fontWeight: 'bold'
            }
          },
          unit: '公顷'
        }
      ]
    }
  },
  mounted () {
    this.fetchData(); // 页面加载时请求数据

    // 设置每300秒重新获取一次数据
    setInterval(this.fetchData, 300000);
  }
}
</script>

<style lang="scss">
#digital-flop {
  position: relative;
  height: 15%;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: rgba(6, 30, 93, 0.5);

  .dv-decoration-10 {
    position: absolute;
    width: 95%;
    left: 2.5%;
    height: 5px;
    bottom: 0;
  }

  .digital-flop-item {
    width: 11%;
    height: 80%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    border-left: 3px solid rgb(6, 30, 93);
    border-right: 3px solid rgb(6, 30, 93);
  }

  .digital-flop-title {
    font-size: 20px;
    margin-bottom: 20px;
  }

  .digital-flop {
    display: flex;
  }

  .unit {
    margin-left: 10px;
    display: flex;
    align-items: flex-end;
    box-sizing: border-box;
    padding-bottom: 13px;
  }
}
</style>
