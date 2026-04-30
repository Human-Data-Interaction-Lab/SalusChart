import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'SalusChart',
  description: 'Modular Jetpack Compose charting library for Android health apps',
  base: '/SalusChart/',

  themeConfig: {
    nav: [
      { text: 'Guide', link: '/guide/getting-started' },
      { text: 'Charts', link: '/charts/' },
      { text: 'Examples', link: '/examples/' },
      { text: 'GitHub', link: 'https://github.com/HDIL-YS/SalusChart' },
    ],

    sidebar: {
      '/guide/': [
        {
          text: 'Guide',
          items: [
            { text: 'Getting Started', link: '/guide/getting-started' },
            { text: 'Installation', link: '/guide/installation' },
            { text: 'Modules', link: '/guide/modules' },
            { text: 'Data Model', link: '/guide/data-model' },
            { text: 'Customization', link: '/guide/customization' },
            { text: 'API Reference', link: '/guide/api-reference' },
          ],
        },
      ],
      '/charts/': [
        {
          text: 'Charts',
          items: [
            { text: 'Overview', link: '/charts/' },
            { text: 'BarChart', link: '/charts/bar-chart' },
            { text: 'LineChart', link: '/charts/line-chart' },
            { text: 'RangeBarChart', link: '/charts/range-bar-chart' },
            { text: 'ScatterPlot', link: '/charts/scatter-plot' },
            { text: 'PieChart', link: '/charts/pie-chart' },
            { text: 'ProgressChart', link: '/charts/progress-chart' },
            { text: 'StackedBarChart', link: '/charts/stacked-bar-chart' },
            { text: 'CalendarChart', link: '/charts/calendar-chart' },
            { text: 'SleepStageChart', link: '/charts/sleep-stage-chart' },
            { text: 'Gauge Charts', link: '/charts/gauge-charts' },
            { text: 'Horizontal Charts', link: '/charts/horizontal-charts' },
            { text: 'Minimal Charts', link: '/charts/minimal-charts' },
            { text: 'Wear OS Charts', link: '/charts/wear-os-charts' },
          ],
        },
      ],
      '/examples/': [
        {
          text: 'Examples',
          items: [
            { text: 'Overview', link: '/examples/' },
            { text: 'Mobile Dashboard', link: '/examples/mobile-dashboard' },
            { text: 'Wear OS', link: '/examples/wear-os' },
          ],
        },
      ],
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/HDIL-YS/SalusChart' },
    ],

    footer: {
      message: 'Released under the Apache 2.0 License.',
      copyright: 'Copyright © 2024 HDILYS',
    },

    search: {
      provider: 'local',
    },
  },
})
