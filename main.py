"""
Voice QA Assistant - Minimal Version (No pyjnius dependency)
"""

from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.utils import platform
import os

if platform != 'android':
    from kivy.core.window import Window
    Window.size = (400, 700)

class VoiceQAApp(App):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.qa_data = []
        self.current_index = 0
        
    def build(self):
        self.title = 'Voice QA Assistant'
        self.main_layout = BoxLayout(orientation='vertical', padding=20, spacing=15)
        
        # Title
        title_label = Label(text='Voice QA Assistant', font_size='24sp', size_hint_y=0.1, bold=True)
        self.main_layout.add_widget(title_label)
        
        # Spacer
        self.main_layout.add_widget(BoxLayout(size_hint_y=0.3))
        
        # Centered button
        button_layout = BoxLayout(size_hint_y=0.15, padding=[50, 0])
        self.excel_button = Button(
            text='Local Excel File',
            font_size='20sp',
            background_color=(0.2, 0.6, 1, 1),
            background_normal='',
            size_hint=(1, 1)
        )
        self.excel_button.bind(on_press=self.show_instructions)
        button_layout.add_widget(self.excel_button)
        self.main_layout.add_widget(button_layout)
        
        # Spacer
        self.main_layout.add_widget(BoxLayout(size_hint_y=0.05))
        
        # Status label
        self.status_label = Label(
            text='Tap button for instructions',
            font_size='16sp',
            size_hint_y=0.1,
            color=(0.5, 0.5, 0.5, 1)
        )
        self.main_layout.add_widget(self.status_label)
        
        # QA Display
        self.qa_layout = BoxLayout(orientation='vertical', size_hint_y=0.4, spacing=10)
        self.qa_layout.opacity = 0
        
        self.question_label = Label(
            text='',
            font_size='18sp',
            text_size=(350, None),
            halign='center',
            valign='middle',
            size_hint_y=0.5
        )
        self.qa_layout.add_widget(self.question_label)
        
        self.answer_label = Label(
            text='',
            font_size='16sp',
            text_size=(350, None),
            halign='center',
            valign='middle',
            size_hint_y=0.5,
            color=(0.3, 0.7, 0.3, 1)
        )
        self.qa_layout.add_widget(self.answer_label)
        
        # Navigation
        nav_layout = BoxLayout(size_hint_y=0.2, spacing=10, padding=[20, 0])
        
        self.prev_button = Button(
            text='< Previous',
            font_size='14sp',
            background_color=(0.4, 0.4, 0.4, 1),
            background_normal=''
        )
        self.prev_button.bind(on_press=self.previous_qa)
        
        self.speak_button = Button(
            text='Speak',
            font_size='14sp',
            background_color=(0.2, 0.8, 0.2, 1),
            background_normal=''
        )
        self.speak_button.bind(on_press=self.speak_current)
        
        self.next_button = Button(
            text='Next >',
            font_size='14sp',
            background_color=(0.4, 0.4, 0.4, 1),
            background_normal=''
        )
        self.next_button.bind(on_press=self.next_qa)
        
        nav_layout.add_widget(self.prev_button)
        nav_layout.add_widget(self.speak_button)
        nav_layout.add_widget(self.next_button)
        self.qa_layout.add_widget(nav_layout)
        
        self.main_layout.add_widget(self.qa_layout)
        self.main_layout.add_widget(BoxLayout(size_hint_y=0.1))
        
        # Load sample data for demo
        self.load_sample_data()
        
        return self.main_layout
    
    def show_instructions(self, instance):
        """Show instructions"""
        self.status_label.text = 'Excel file feature coming soon!\nUsing sample Q&A for now'
        if self.qa_data:
            self.qa_layout.opacity = 1
            self.show_current_qa()
    
    def load_sample_data(self):
        """Load sample Q&A data"""
        self.qa_data = [
            {'question': 'What is your name?', 'answer': 'I am Voice QA Assistant'},
            {'question': 'How are you?', 'answer': 'I am doing great, thank you!'},
            {'question': 'What can you do?', 'answer': 'I can display Q&A pairs from Excel files'}
        ]
        self.current_index = 0
    
    def show_current_qa(self):
        """Display current Q&A"""
        if self.qa_data:
            qa = self.qa_data[self.current_index]
            self.question_label.text = f'Q: {qa["question"]}'
            self.answer_label.text = f'A: {qa["answer"]}'
            self.status_label.text = f'Q&A {self.current_index + 1} of {len(self.qa_data)}'
    
    def previous_qa(self, instance):
        """Go to previous Q&A"""
        if self.qa_data and self.current_index > 0:
            self.current_index -= 1
            self.show_current_qa()
    
    def next_qa(self, instance):
        """Go to next Q&A"""
        if self.qa_data and self.current_index < len(self.qa_data) - 1:
            self.current_index += 1
            self.show_current_qa()
    
    def speak_current(self, instance):
        """Speak current Q&A"""
        if not self.qa_data:
            return
        qa = self.qa_data[self.current_index]
        text = f"{qa['question']}. {qa['answer']}"
        
        # Android TTS using native Java/Kotlin approach
        if platform == 'android':
            try:
                from android.runnable import run_on_ui_thread
                from jnius import autoclass
                
                @run_on_ui_thread
                def speak():
                    PythonActivity = autoclass('org.kivy.android.PythonActivity')
                    TextToSpeech = autoclass('android.speech.tts.TextToSpeech')
                    Locale = autoclass('java.util.Locale')
                    
                    context = PythonActivity.mActivity
                    tts = TextToSpeech(context, None)
                    tts.setLanguage(Locale.US)
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, None, None)
                
                speak()
            except Exception as e:
                self.status_label.text = f'TTS not available: {str(e)}'
        else:
            self.status_label.text = 'TTS only works on Android device'

if __name__ == '__main__':
    VoiceQAApp().run()
